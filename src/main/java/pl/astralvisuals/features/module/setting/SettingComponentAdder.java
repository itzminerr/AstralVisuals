package pl.astralvisuals.features.module.setting;

import java.util.List;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.AbstractSettingComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.BindComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.CheckboxComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.ColorComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.GroupComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.SButtonComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.SliderComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.TextComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.multiselect.MultiSelectComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.select.SelectComponent;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ButtonSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.GroupSetting;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.features.module.setting.implement.TextSetting;

public class SettingComponentAdder {
   public void addSettingComponent(List<Setting> settings, List<AbstractSettingComponent> components) {
      settings.forEach(setting -> {
         if (setting instanceof BooleanSetting booleanSetting) {
            components.add(new CheckboxComponent(booleanSetting));
         }

         if (setting instanceof BindSetting bindSetting) {
            components.add(new BindComponent(bindSetting));
         }

         if (setting instanceof ColorSetting colorSetting) {
            components.add(new ColorComponent(colorSetting));
         }

         if (setting instanceof TextSetting textSetting) {
            components.add(new TextComponent(textSetting));
         }

         if (setting instanceof SliderSettings valueSetting) {
            components.add(new SliderComponent(valueSetting));
         }

         if (setting instanceof GroupSetting groupSetting) {
            components.add(new GroupComponent(groupSetting));
         }

         if (setting instanceof ButtonSetting buttonSetting) {
            components.add(new SButtonComponent(buttonSetting));
         }

         if (setting instanceof SelectSetting selectSetting) {
            components.add(new SelectComponent(selectSetting));
         }

         if (setting instanceof MultiSelectSetting multiSelectSetting) {
            components.add(new MultiSelectComponent(multiSelectSetting));
         }
      });
   }
}
