package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import java.awt.Color;
import net.minecraft.class_332;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.CheckComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.other.SettingComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.AbstractWindow;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.group.GroupWindow;
import pl.astralvisuals.features.module.setting.implement.GroupSetting;
import pl.astralvisuals.utils.display.font.Fonts;

public class GroupComponent extends AbstractSettingComponent {
   private final CheckComponent checkComponent = new CheckComponent();
   private final SettingComponent settingComponent = new SettingComponent();
   private final GroupSetting setting;

   public GroupComponent(GroupSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      this.height = 15.0F;
      Fonts.getSize(20, Fonts.Type.GUIICONS).drawString(context.method_51448(), "K", this.x + 6.0F, this.y + 10.0F, new Color(128, 128, 128, 64).getRGB());
      Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.method_51448(), this.setting.getName(), this.x + 20.0F, this.y + 11.0F, -2828575);
      this.checkComponent
         .position(this.x + this.width - 19.0F, this.y + 6.5F)
         .setRunnable(() -> this.setting.setValue(!this.setting.isValue()))
         .setState(this.setting.isValue())
         .render(context, mouseX, mouseY, delta);
      ((SettingComponent)this.settingComponent.position(this.x + this.width - 31.0F, this.y + 6.0F))
         .setRunnable(() -> this.spawnWindow(mouseX, mouseY))
         .render(context, mouseX, mouseY, delta);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.checkComponent.mouseClicked(mouseX, mouseY, button);
      this.settingComponent.mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void spawnWindow(int mouseX, int mouseY) {
      AbstractWindow existingWindow = null;

      for (AbstractWindow window : windowManager.getWindows()) {
         if (window instanceof GroupWindow && ((GroupWindow)window).getSetting() == this.setting) {
            existingWindow = window;
            break;
         }
      }

      if (existingWindow != null) {
         windowManager.delete(existingWindow);
      } else {
         AbstractWindow groupWindow = new GroupWindow(this.setting).position(mouseX + 10, mouseY).size(137.0F, 23.0F).draggable(false);
         windowManager.add(groupWindow);
      }
   }
}
