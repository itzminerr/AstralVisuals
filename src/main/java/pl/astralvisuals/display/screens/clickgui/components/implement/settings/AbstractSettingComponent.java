package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.setting.Setting;

public abstract class AbstractSettingComponent extends AbstractComponent {
   private final Setting setting;

   public Setting getSetting() {
      return this.setting;
   }

   public AbstractSettingComponent(Setting setting) {
      this.setting = setting;
   }
}
