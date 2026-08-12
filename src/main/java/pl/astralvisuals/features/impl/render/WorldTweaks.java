package pl.astralvisuals.features.impl.render;

import java.awt.Color;
import pl.astralvisuals.events.render.FogEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class WorldTweaks extends Module {
   public static final String BRIGHTNESS = "Яркость";
   public static final String TIME = "Время";
   public static final String FOG = "Туман";
   public final MultiSelectSetting modeSetting = new MultiSelectSetting("Настройки мира", "Визуальные настройки мира").value("Яркость", "Время", "Туман");
   public final SliderSettings brightSetting = new SliderSettings("Яркость", "Максимальный уровень яркости")
      .setValue(1.0F)
      .range(0.0F, 1.0F)
      .visible(() -> this.modeSetting.isSelected("Яркость"));
   public final SliderSettings timeSetting = new SliderSettings("Время", "Значение времени мира")
      .setValue(12.0F)
      .range(0, 24)
      .visible(() -> this.modeSetting.isSelected("Время"));
   public final SliderSettings distanceSetting = new SliderSettings("Дистанция тумана", "Дистанция рендера тумана")
      .setValue(100.0F)
      .range(20, 200)
      .visible(() -> this.modeSetting.isSelected("Туман"));
   public final ColorSetting fogColorSetting = new ColorSetting("Цвет тумана", "Цвет тумана")
      .setColor(new Color(225, 225, 255, 255).getRGB())
      .presets(new Color(225, 225, 255, 255).getRGB(), -1, -16777216, -16711936, -16776961, -65536)
      .visible(() -> this.modeSetting.isSelected("Туман"));

   public static WorldTweaks getInstance() {
      return Instance.get(WorldTweaks.class);
   }

   public WorldTweaks() {
      super("WorldTweaks", "World Tweaks", ModuleCategory.RENDER);
      this.setup(this.modeSetting, this.brightSetting, this.timeSetting, this.distanceSetting, this.fogColorSetting);
   }

   @Override
   public void deactivate() {
      super.deactivate();
   }

   @EventHandler
   public void onFog(FogEvent e) {
      if (this.modeSetting.isSelected("Туман")) {
         e.setDistance(this.distanceSetting.getValue());
         e.setColor(this.fogColorSetting.getColor());
         e.cancel();
      }
   }
}
