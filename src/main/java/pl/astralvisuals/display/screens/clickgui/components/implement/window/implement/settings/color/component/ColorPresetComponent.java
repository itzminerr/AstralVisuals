package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_332;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.ColorPresetButton;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.font.Fonts;

public class ColorPresetComponent extends AbstractComponent {
   private final List<ColorPresetButton> colorPresetButtonList = new ArrayList<>();
   private final ColorSetting setting;
   private float windowHeight;
   private float windowWidth;

   public ColorPresetComponent(ColorSetting setting) {
      this.setting = setting;

      for (int preset : setting.getPresets()) {
         this.colorPresetButtonList.add(new ColorPresetButton(setting, preset));
      }
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      if (!this.colorPresetButtonList.isEmpty()) {
         Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(context.method_51448(), "Пресеты", this.x + 8.0F, this.y + 127.0F, -1);
      }

      int xOffset = 0;
      int yOffset = 0;
      int colorIndex = 0;
      int size = 12;
      int columns = Math.max(1, (int)((this.width - 16.0F) / size));

      for (ColorPresetButton button : this.colorPresetButtonList) {
         button.x = this.x + 8.0F + xOffset;
         button.y = this.y + 135.0F + yOffset;
         button.render(context, mouseX, mouseY, delta);
         xOffset += size;
         if (++colorIndex >= columns) {
            colorIndex = 0;
            xOffset = 0;
            yOffset += size - 1;
         }
      }

      this.windowHeight = this.colorPresetButtonList.isEmpty() ? 124.0F : 152 + yOffset;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.colorPresetButtonList.forEach(colorPresetButton -> colorPresetButton.mouseClicked(mouseX, mouseY, button));
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public List<ColorPresetButton> getColorPresetButtonList() {
      return this.colorPresetButtonList;
   }

   public ColorSetting getSetting() {
      return this.setting;
   }

   public float getWindowHeight() {
      return this.windowHeight;
   }

   public float getWindowWidth() {
      return this.windowWidth;
   }
}
