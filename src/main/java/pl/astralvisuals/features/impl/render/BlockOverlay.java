package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2960;
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

   public static BlockOverlay getInstance() {
      return Instance.get(BlockOverlay.class);
   }

   public BlockOverlay() {
      super("BlockOverlay", "Block Overlay", ModuleCategory.RENDER);
      this.setup(this.typeSetting, this.linesSetting, this.colorSetting, this.lineWidth,
         this.shaderMode, this.shaderColor, this.speed, this.scale, this.intensity, this.alpha);
   }

   @Override
   public void deactivate() {
      super.deactivate();
      this.startMillis = -1L;
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

      if (this.typeSetting.isSelected(SHADER)) {
         this.renderShaderFaces(pos, shape);
         if (lines) {
            Render3D.drawShapeAlternative(pos, shape, this.colorSetting.getColor(), width, false, true);
         }
      } else if (lines) {
         // Заливка + линии (исходный вид Block Overlay).
         Render3D.drawShapeAlternative(pos, shape, this.colorSetting.getColor(), width, true, true);
      } else {
         // Только заливка, без линий.
         List<class_238> boxes = shape.method_1090();
         for (class_238 relative : boxes) {
            Render3D.drawBox(relative.method_996(pos), this.colorSetting.getColor(), 2.0F, false, true, true);
         }
      }
   }

   // Заливает грани блока шейдером (своими настройками, core/sky/water | caustic).
   private void renderShaderFaces(class_2338 pos, class_265 shape) {
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

      for (class_238 relative : shape.method_1090()) {
         this.appendBoxFaces(buffer, matrix, relative.method_996(pos).method_1014(0.002));
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
