package pl.astralvisuals.features.impl.render;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_293.class_5596;
import net.minecraft.class_2960;
import net.minecraft.class_4184;
import net.minecraft.class_5944;
import org.lwjgl.opengl.GL11;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;

// Sky Shader: тот же узор воды/каустики, что и в Block Overlay (режим «Шейдер»).
// Рендерится через событие Fabric AFTER_TRANSLUCENT — Sodium-совместимая точка, где буфер
// глубины валиден и работает аппаратный depth-тест. Квад рисуется на ДАЛЬНЕЙ плоскости (NDC z=1.0)
// с тестом LEQUAL: реальная геометрия (depth < 1.0) перекрывает его => узор виден ТОЛЬКО на небе.
public class SkyShader extends Module {
   public static final String WATER = "Вода";
   public static final String CAUSTIC = "Каустика";

   private static final class_10156 SKY_SHADER = new class_10156(
      class_2960.method_60654("core/sky/sky_shader"), class_290.field_1592, class_10149.field_53930);

   // Настройки идентичны шейдерным настройкам Block Overlay.
   private final SelectSetting shaderMode = new SelectSetting("Режим шейдера", "Тип шейдера")
      .value(WATER, CAUSTIC)
      .selected(WATER);

   private final ColorSetting shaderColor = new ColorSetting("Цвет шейдера", "Цвет шейдера")
      .value(new Color(70, 140, 255).getRGB());

   private final SliderSettings speed = new SliderSettings("Скорость", "Скорость анимации")
      .setValue(1.0F).range(0.1F, 5.0F);

   private final SliderSettings scale = new SliderSettings("Размер", "Масштаб узора")
      .setValue(5.0F).range(1.0F, 20.0F);

   private final SliderSettings intensity = new SliderSettings("Интенсивность", "Контраст узора")
      .setValue(0.01F).range(0.001F, 0.05F);

   private final SliderSettings alpha = new SliderSettings("Прозрачность", "Непрозрачность шейдера")
      .setValue(1.0F).range(0.3F, 1.0F);

   private long startMillis = -1L;

   public static SkyShader getInstance() {
      return Instance.get(SkyShader.class);
   }

   public SkyShader() {
      super("SkyShader", "Sky Shader", ModuleCategory.RENDER);
      this.setup(this.shaderMode, this.shaderColor, this.speed, this.scale, this.intensity, this.alpha);

      // Fabric-событие после отрисовки полупрозрачного террейна: глубина валидна (в т.ч. с Sodium).
      WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
         if (this.isState()) {
            this.render(context.camera());
         }
      });
   }

   @Override
   public void deactivate() {
      super.deactivate();
      this.startMillis = -1L;
   }

   private void render(class_4184 camera) {
      if (mc.field_1687 == null || camera == null) {
         return;
      }

      class_5944 shader = RenderSystem.setShader(SKY_SHADER);
      if (shader == null) {
         return;
      }

      if (this.startMillis < 0L) {
         this.startMillis = System.currentTimeMillis();
      }

      float time = (System.currentTimeMillis() - this.startMillis) / 1000.0F;
      float fw = mc.method_22683().method_4489();
      float fh = mc.method_22683().method_4506();

      int rgb = this.shaderColor.getColor();
      float cr = ((rgb >> 16) & 0xFF) / 255.0F;
      float cg = ((rgb >> 8) & 0xFF) / 255.0F;
      float cb = (rgb & 0xFF) / 255.0F;

      float yawRad = (float) Math.toRadians(-camera.method_19330());
      float pitchRad = (float) Math.toRadians(camera.method_19329());
      float fov = ((Integer) mc.field_1690.method_41808().method_41753()).floatValue();

      shader.method_34582("uTime").method_1251(time);
      shader.method_34582("uResolution").method_1255(fw, fh);
      shader.method_34582("uColor").method_1249(cr, cg, cb);
      shader.method_34582("uAlpha").method_1251(this.alpha.getValue());
      shader.method_34582("uSpeed").method_1251(this.speed.getValue() / 10.0F);
      shader.method_34582("uScale").method_1251(this.scale.getValue());
      shader.method_34582("uIntensity").method_1251(this.intensity.getValue());
      shader.method_34582("uCameraDir").method_1255(yawRad, pitchRad);
      shader.method_34582("uFov").method_1251(fov);
      shader.method_34582("uMode").method_1251(this.shaderMode.isSelected(CAUSTIC) ? 1.0F : 0.0F);

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      // Аппаратный depth-тест: квад почти на дальней плоскости перекрывается любой геометрией.
      // z = 0.9999 (а не ровно 1.0): на самой дальней плоскости depth небо == 1.0, и при ровно 1.0
      // сравнение LEQUAL «мигает» из-за погрешности — узор иногда пропадал. 0.9999 <= 1.0 проходит
      // стабильно, но всё ещё перекрывается любой реальной геометрией (depth < 0.9999).
      RenderSystem.enableDepthTest();
      RenderSystem.depthFunc(GL11.GL_LEQUAL);
      RenderSystem.depthMask(false);
      RenderSystem.disableCull();

      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1592);
      buffer.method_22912(-1.0F, -1.0F, 0.9999F);
      buffer.method_22912(1.0F, -1.0F, 0.9999F);
      buffer.method_22912(1.0F, 1.0F, 0.9999F);
      buffer.method_22912(-1.0F, 1.0F, 0.9999F);
      class_286.method_43433(buffer.method_60800());

      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
