package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class SaturationComponent extends AbstractComponent {
   private final ColorSetting setting;
   private boolean saturationDragging;
   private float X;
   private float Y;
   private float W;
   private float H;

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.X = this.x + 8.0F;
      this.Y = this.y + 92.0F;
      this.W = this.width - 16.0F;
      this.H = 6.0F;
      float handleSize = 8.0F;
      float clampedX = class_3532.method_15363(this.X + this.W * this.setting.getHue() - handleSize / 2.0F, this.X, this.X + this.W - handleSize);
      image.setTexture("textures/gui/sliderhue.png").render(ShapeProperties.create(matrix, this.X, this.Y, this.W, this.H).round(this.H / 2.0F).build());
      rectangle.render(
         ShapeProperties.create(matrix, clampedX, this.Y - 1.0F, handleSize, handleSize)
            .round(handleSize / 2.0F)
            .thickness(2.0F)
            .color(16777215)
            .outlineColor(-1)
            .build()
      );
      if (this.saturationDragging) {
         this.applyValue(mouseX);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.saturationDragging = button == 0 && Calculate.isHovered(mouseX, mouseY, this.X, this.Y, this.W, this.H);
      if (this.saturationDragging) {
         this.applyValue(mouseX);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.saturationDragging = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.saturationDragging && button == 0) {
         this.applyValue(mouseX);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   private void applyValue(double mouseX) {
      this.setting.setHue(class_3532.method_15363((float)(mouseX - this.X) / this.W, 0.0F, 1.0F));
   }

   public SaturationComponent(ColorSetting setting) {
      this.setting = setting;
   }
}
