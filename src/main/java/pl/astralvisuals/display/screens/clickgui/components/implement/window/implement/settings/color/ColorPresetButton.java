package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color;

import net.minecraft.class_332;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ColorPresetButton extends AbstractComponent {
   private final ColorSetting setting;
   private final int color;

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      rectangle.render(ShapeProperties.create(context.method_51448(), this.x, this.y, 9.0, 9.0).round(3.0F).color(this.color).build());
      rectangle.render(
         ShapeProperties.create(context.method_51448(), this.x, this.y, 9.0, 9.0)
            .round(3.0F)
            .thickness(0.6F)
            .outlineColor(ColorAssist.getOutline())
            .color(0)
            .build()
      );
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, 9.0, 9.0) && button == 0) {
         this.setting.setColor(this.color);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public ColorPresetButton(ColorSetting setting, int color) {
      this.setting = setting;
      this.color = color;
   }
}
