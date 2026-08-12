package pl.astralvisuals.features.impl.render;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;

public class HitColor extends Module {
   private final ColorSetting colorSetting = new ColorSetting("Цвет", "Цвет вспышки при ударе")
      .setColor(-16711681)
      .presets(-1, -65536, -23178, -16711936, -16776961, -7722014);
   private final SliderSettings alphaSetting = new SliderSettings("Прозрачность", "Непрозрачность вспышки урона (%)")
      .setValue(70.0F)
      .range(0, 100);

   public static HitColor getInstance() {
      return Instance.get(HitColor.class);
   }

   public HitColor() {
      super("HitColor", "Hit Color", ModuleCategory.RENDER);
      this.setup(this.colorSetting, this.alphaSetting);
   }

   // ARGB: RGB из настройки цвета + альфа из ползунка (0..100% -> 0..255).
   // Перекраску текстуры оверлея покадрово выполняет GameRendererMixin -> OverlayTextureMixin.
   public int getColor() {
      int alpha = Math.round(this.alphaSetting.getValue() / 100.0F * 255.0F);
      return (alpha & 0xFF) << 24 | this.colorSetting.getColor() & 0xFFFFFF;
   }
}
