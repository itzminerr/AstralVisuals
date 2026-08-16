package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_265;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_3965;
import net.minecraft.class_239.class_240;
import net.minecraft.class_4184;
import net.minecraft.class_5944;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.geometry.Render3D;

public class BlockOverlay extends Module {
   public static final String STATIC = "Статичный";
   public static final String SHADER = "Шейдер";
   public static final String WATER = "Вода";
   public static final String CAUSTIC = "Каустика";

   private static final class_10156 WATER_SHADER = new class_10156(
      class_2960.method_60654("core/sky/water"), class_290.field_1592, class_10149.field_53930);

   private static final class_10156 CAUSTIC_SHADER = new class_10156(
      class_2960.method_60654("core/sky/caustic"), class_290.field_1592, class_10149.field_53930);

   private final SelectSetting typeSetting = new SelectSetting("Тип", "Тип подсветки блока")
      .value(STATIC, SHADER)
      .selected(STATIC);

   private final BooleanSetting linesSetting = new BooleanSetting("Линии по краям", "Рисовать линии по краям блока")
      .setValue(true);

   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет заливки и линий")
      .setColor(-1)
      .presets(-1, -9659651, -7569409, -23178, -33925);

   private final SliderSettings lineWidth = new SliderSettings("Ширина линий", "Толщина рёбер блока")
      .setValue(2.0F).range(0.5F, 5.0F)
      .visible(() -> this.linesSetting.isValue());

   private final BooleanSetting smoothTransition = new BooleanSetting("Плавный переход", "Плавно перемещать контур между блоками")
      .setValue(true);

   private final SliderSettings transitionTime = new SliderSettings("Время перехода", "Длительность перехода между блоками в секундах")
      .setValue(0.18F).range(0.05F, 1.0F)
      .visible(this.smoothTransition::isValue);

   // Настройки шейдера (отдельные от Sky Shader), видимы только в режиме «Шейдер».
   private final SelectSetting shaderMode = new SelectSetting("Режим шейдера", "Тип шейдера")
      .value(WATER, CAUSTIC)
      .selected(WATER)
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private final ColorSetting shaderColor = new ColorSetting("Цвет шейдера", "Цвет шейдера")
      .value(new Color(70, 140, 255).getRGB())
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private final SliderSettings speed = new SliderSettings("Скорость", "Скорость анимации")
      .setValue(1.0F).range(0.1F, 5.0F)
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private final SliderSettings scale = new SliderSettings("Размер", "Масштаб узора")
      .setValue(5.0F).range(1.0F, 20.0F)
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private final SliderSettings intensity = new SliderSettings("Интенсивность", "Контраст узора")
      .setValue(0.01F).range(0.001F, 0.05F)
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private final SliderSettings alpha = new SliderSettings("Прозрачность", "Непрозрачность шейдера")
      .setValue(1.0F).range(0.3F, 1.0F)
      .visible(() -> this.typeSetting.isSelected(SHADER));

   private long startMillis = -1L;
   private class_2338 lastTargetPos;
   private List<class_238> animationFromBoxes = List.of();
   private List<class_238> animationToBoxes = List.of();
   private long animationStartedAt;

   public static BlockOverlay getInstance() {
      return Instance.get(BlockOverlay.class);
   }

   public BlockOverlay() {
      super("BlockOverlay", "Block Overlay", ModuleCategory.RENDER);
      this.setup(this.typeSetting, this.linesSetting, this.colorSetting, this.lineWidth, this.smoothTransition, this.transitionTime,
         this.shaderMode, this.shaderColor, this.speed, this.scale, this.intensity, this.alpha);
   }

   @Override
   public void deactivate() {
      super.deactivate();
      this.startMillis = -1L;
      this.resetTransition();
   }

   @EventHandler
   public void onWorldRender(WorldRenderEvent e) {
      if (!(mc.field_1765 instanceof class_3965 result) || !result.method_17783().equals(class_240.field_1332)) {
         return;
      }

      class_2338 pos = result.method_17777();
      class_265 shape = mc.field_1687.method_8320(pos).method_26218(mc.field_1687, pos);
      if (shape.method_1110()) {
         return;
      }

      boolean lines = this.linesSetting.isValue();

      float width = this.lineWidth.getValue();
      List<class_238> targetBoxes = shape.method_1090().stream().map(box -> box.method_996(pos)).toList();
      this.updateTransition(pos, targetBoxes);
      List<class_238> currentBoxes = this.currentBoxes(System.currentTimeMillis());

      if (this.typeSetting.isSelected(SHADER)) {
         this.renderShaderFaces(currentBoxes);
         if (lines) {
            for (class_238 box : currentBoxes) {
               Render3D.drawBox(box, this.colorSetting.getColor(), width, true, false, true);
            }
         }
      } else {
         for (class_238 box : currentBoxes) {
            Render3D.drawBox(box, this.colorSetting.getColor(), lines ? width : 2.0F, lines, true, true);
         }
      }
   }

   private void updateTransition(class_2338 position, List<class_238> boxes) {
      long now = System.currentTimeMillis();
      boolean changed = this.lastTargetPos == null || !this.lastTargetPos.equals(position) || !this.sameBoxes(this.animationToBoxes, boxes);
      if (!this.smoothTransition.isValue()) {
         this.lastTargetPos = position.method_10062();
         this.animationFromBoxes = new java.util.ArrayList<>(boxes);
         this.animationToBoxes = new java.util.ArrayList<>(boxes);
         this.animationStartedAt = now;
         return;
      }
      if (!changed) {
         return;
      }
      this.animationFromBoxes = this.animationToBoxes.isEmpty() ? new java.util.ArrayList<>(boxes) : this.currentBoxes(now);
      this.animationToBoxes = new java.util.ArrayList<>(boxes);
      this.animationStartedAt = now;
      this.lastTargetPos = position.method_10062();
   }

   private List<class_238> currentBoxes(long now) {
      if (this.animationToBoxes.isEmpty() || !this.smoothTransition.isValue()) {
         return this.animationToBoxes;
      }
      float duration = this.transitionTime.getValue();
      if (duration <= 0.0F || this.animationFromBoxes.isEmpty()) {
         return this.animationToBoxes;
      }
      float progress = class_3532.method_15363((now - this.animationStartedAt) / (duration * 1000.0F), 0.0F, 1.0F);
      if (progress >= 1.0F) {
         this.animationFromBoxes = this.animationToBoxes;
         return this.animationToBoxes;
      }
      int size = Math.max(this.animationFromBoxes.size(), this.animationToBoxes.size());
      List<class_238> result = new java.util.ArrayList<>(size);
      class_238 fromFallback = this.combined(this.animationFromBoxes);
      class_238 toFallback = this.combined(this.animationToBoxes);
      for (int index = 0; index < size; index++) {
         class_238 from = index < this.animationFromBoxes.size() ? this.animationFromBoxes.get(index) : fromFallback;
         class_238 to = index < this.animationToBoxes.size() ? this.animationToBoxes.get(index) : toFallback;
         if (from != null && to != null) {
            result.add(this.lerp(from, to, progress));
         }
      }
      return result;
   }

   private class_238 combined(List<class_238> boxes) {
      if (boxes.isEmpty()) {
         return null;
      }
      class_238 result = boxes.get(0);
      for (int index = 1; index < boxes.size(); index++) {
         result = result.method_991(boxes.get(index));
      }
      return result;
   }

   private class_238 lerp(class_238 from, class_238 to, float progress) {
      return new class_238(
         class_3532.method_16436(progress, from.field_1323, to.field_1323),
         class_3532.method_16436(progress, from.field_1322, to.field_1322),
         class_3532.method_16436(progress, from.field_1321, to.field_1321),
         class_3532.method_16436(progress, from.field_1320, to.field_1320),
         class_3532.method_16436(progress, from.field_1325, to.field_1325),
         class_3532.method_16436(progress, from.field_1324, to.field_1324)
      );
   }

   private boolean sameBoxes(List<class_238> first, List<class_238> second) {
      if (first.size() != second.size()) {
         return false;
      }
      for (int index = 0; index < first.size(); index++) {
         if (!first.get(index).equals(second.get(index))) {
            return false;
         }
      }
      return true;
   }

   private void resetTransition() {
      this.lastTargetPos = null;
      this.animationFromBoxes = List.of();
      this.animationToBoxes = List.of();
      this.animationStartedAt = 0L;
   }

   // Заливает грани блока шейдером (своими настройками, core/sky/water | caustic).
   private void renderShaderFaces(List<class_238> boxes) {
      if (this.startMillis < 0L) {
         this.startMillis = System.currentTimeMillis();
      }

      class_10156 key = this.shaderMode.isSelected(CAUSTIC) ? CAUSTIC_SHADER : WATER_SHADER;
      class_5944 shader = RenderSystem.setShader(key);
      if (shader == null) {
         return;
      }

      float time = (System.currentTimeMillis() - this.startMillis) / 1000.0F;
      float fw = mc.method_22683().method_4489();
      float fh = mc.method_22683().method_4506();

      int themeColor = this.shaderColor.getColor();
      float cr = ((themeColor >> 16) & 0xFF) / 255.0F;
      float cg = ((themeColor >> 8) & 0xFF) / 255.0F;
      float cb = (themeColor & 0xFF) / 255.0F;

      class_4184 cam = mc.field_1773.method_19418();
      float yawRad = (float) Math.toRadians(-cam.method_19330());
      float pitchRad = (float) Math.toRadians(cam.method_19329());
      float fov = ((Integer) mc.field_1690.method_41808().method_41753()).floatValue();

      shader.method_34582("uTime").method_1251(time);
      shader.method_34582("uResolution").method_1255(fw, fh);
      shader.method_34582("uColor").method_1249(cr, cg, cb);
      shader.method_34582("uAlpha").method_1251(this.alpha.getValue());
      shader.method_34582("uSpeed").method_1251(this.speed.getValue());
      shader.method_34582("uScale").method_1251(this.scale.getValue());
      shader.method_34582("uIntensity").method_1251(this.intensity.getValue());
      shader.method_34582("uCameraDir").method_1255(yawRad, pitchRad);
      shader.method_34582("uFov").method_1251(fov);

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.depthFunc(GL11.GL_LEQUAL);
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();

      Matrix4f matrix = Render3D.lastWorldSpaceMatrix.method_23761();
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1592);

      for (class_238 box : boxes) {
         this.appendBoxFaces(buffer, matrix, box.method_1014(0.002));
      }

      class_286.method_43433(buffer.method_60800());

      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void appendBoxFaces(class_287 buffer, Matrix4f m, class_238 box) {
      float x1 = (float) box.field_1323;
      float y1 = (float) box.field_1322;
      float z1 = (float) box.field_1321;
      float x2 = (float) box.field_1320;
      float y2 = (float) box.field_1325;
      float z2 = (float) box.field_1324;

      // bottom
      buffer.method_22918(m, x1, y1, z1);
      buffer.method_22918(m, x2, y1, z1);
      buffer.method_22918(m, x2, y1, z2);
      buffer.method_22918(m, x1, y1, z2);
      // top
      buffer.method_22918(m, x1, y2, z1);
      buffer.method_22918(m, x1, y2, z2);
      buffer.method_22918(m, x2, y2, z2);
      buffer.method_22918(m, x2, y2, z1);
      // north
      buffer.method_22918(m, x1, y1, z1);
      buffer.method_22918(m, x1, y2, z1);
      buffer.method_22918(m, x2, y2, z1);
      buffer.method_22918(m, x2, y1, z1);
      // south
      buffer.method_22918(m, x1, y1, z2);
      buffer.method_22918(m, x2, y1, z2);
      buffer.method_22918(m, x2, y2, z2);
      buffer.method_22918(m, x1, y2, z2);
      // west
      buffer.method_22918(m, x1, y1, z1);
      buffer.method_22918(m, x1, y1, z2);
      buffer.method_22918(m, x1, y2, z2);
      buffer.method_22918(m, x1, y2, z1);
      // east
      buffer.method_22918(m, x2, y1, z1);
      buffer.method_22918(m, x2, y2, z1);
      buffer.method_22918(m, x2, y2, z2);
      buffer.method_22918(m, x2, y1, z2);
   }
}
