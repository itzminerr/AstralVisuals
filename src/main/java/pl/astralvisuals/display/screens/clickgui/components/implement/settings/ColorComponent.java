package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.display.screens.clickgui.MenuScreen;
import pl.astralvisuals.display.screens.clickgui.components.implement.module.ModuleSettingsWindow;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.AbstractWindow;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.ColorWindow;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ColorComponent extends AbstractSettingComponent {
   private final ColorSetting setting;

   public ColorComponent(ColorSetting setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.height = 20.0F;
      Fonts.getSize(13, Fonts.Type.DEFAULT).drawString(context.method_51448(), this.setting.getName(), this.x + 8.0F, this.y + 12.0F, -2828575);
      rectangle.render(ShapeProperties.create(matrix, this.x + this.width - 18.0F, this.y + 10.0F, 7.0, 7.0).round(3.5F).color(this.setting.getColor()).build());
      rectangle.render(
         ShapeProperties.create(matrix, this.x + this.width - 18.0F, this.y + 10.0F, 7.0, 7.0)
            .round(3.5F)
            .thickness(2.0F)
            .softness(1.0F)
            .outlineColor(ColorAssist.getText())
            .color(16777215)
            .build()
      );
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      ColorWindow currentWindow = null;
      List<AbstractWindow> otherColorWindows = new ArrayList<>();

      for (AbstractWindow window : windowManager.getWindows()) {
         if (window instanceof ColorWindow colorWindow) {
            if (colorWindow.getSetting() == this.setting) {
               currentWindow = colorWindow;
            } else {
               otherColorWindows.add(colorWindow);
            }
         }
      }

      if (button == 1 && currentWindow != null) {
         windowManager.delete(currentWindow);
         return true;
      } else if (button != 0 || !this.isToggleHovered(mouseX, mouseY)) {
         return super.mouseClicked(mouseX, mouseY, button);
      } else if (currentWindow != null) {
         windowManager.delete(currentWindow);
         return true;
      } else {
         otherColorWindows.forEach(windowManager::delete);
         MenuScreen menu = MenuScreen.INSTANCE;
         AbstractWindow parentWindow = this.findParentSettingsWindow();
         float popupWidth = 118.0F;
         float popupHeight = ColorWindow.getPreferredHeight(this.setting);
         float popupX = parentWindow == null ? this.x + this.width + 10.0F : parentWindow.x + parentWindow.width + 10.0F;
         float popupY = parentWindow == null ? this.y : parentWindow.y;
         popupY = Math.max((float)(menu.y - 5), Math.min(popupY, menu.y + menu.height - popupHeight));
         AbstractWindow colorWindowx = new ColorWindow(this.setting).position(popupX, popupY).size(popupWidth, popupHeight).draggable(true);
         windowManager.add(colorWindowx);
         return true;
      }
   }

   private AbstractWindow findParentSettingsWindow() {
      for (AbstractWindow window : windowManager.getWindows()) {
         if (window instanceof ModuleSettingsWindow
            && this.x >= window.x - 1.0F
            && this.x <= window.x + window.width + 1.0F
            && this.y >= window.y
            && this.y <= window.y + window.height + 10.0F) {
            return window;
         }
      }

      return null;
   }

   private boolean isToggleHovered(double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, this.x + 4.0F, this.y + 4.0F, this.width - 8.0F, 12.0);
   }
}
