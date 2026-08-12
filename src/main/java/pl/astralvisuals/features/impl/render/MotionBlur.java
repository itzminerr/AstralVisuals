package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.impl.render.motionblur.ShaderMotionBlur;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;

// Порт Motion Blur из EvaWare (на Satin). Захват матриц кадра — в MotionBlurMixin (class_761).
public class MotionBlur extends Module {
   private final SliderSettings strength = new SliderSettings("Сила", "Сила размытия в движении").setValue(-0.8F).range(-2.0F, 2.0F);
   private final BooleanSetting refreshScaling = new BooleanSetting("Масштаб по частоте", "Учитывать частоту обновления монитора").setValue(true);
   private ShaderMotionBlur shader;

   public static MotionBlur getInstance() {
      return Instance.get(MotionBlur.class);
   }

   public MotionBlur() {
      super("MotionBlur", "Motion Blur", ModuleCategory.RENDER);
      this.setup(this.strength, this.refreshScaling);
   }

   // Ленивая инициализация: ManagedShaderEffect создаём при первом кадре (рендер-поток, ресурсы готовы).
   public ShaderMotionBlur getShader() {
      if (this.shader == null) {
         this.shader = new ShaderMotionBlur(this);
      }

      return this.shader;
   }

   public float getStrength() {
      return this.strength.getValue();
   }

   public boolean useRefreshRateScaling() {
      return this.refreshScaling.isValue();
   }
}
