package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.settings.color.component;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class HueComponent extends AbstractComponent {
   private final ColorSetting setting;
   private boolean hueDragging;
   private float X;
   private float Y;
   private float W;
   private float H;

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.X = this.x + 8.0F;
      this.Y = this.y + 24.0F;
      this.W = this.width - 16.0F;
      this.H = 58.0F;
      int[] color = new int[]{-16777216, -1, -16777216, Color.HSBtoRGB(this.setting.getHue(), 1.0F, 1.0F)};
      rectangle.render(ShapeProperties.create(matrix, this.X, this.Y, this.W, this.H).round(6.0F).color(color).build());
      float handleSize = 6.0F;
      float clampedX = class_3532.method_15363(this.X + this.W * this.setting.getSaturation() - handleSize / 2.0F, this.X, this.X + this.W - handleSize);
      float clampedY = class_3532.method_15363(
         this.Y + this.H * (1.0F - this.setting.getBrightness()) - handleSize / 2.0F, this.Y, this.Y + this.H - handleSize
      );
      rectangle.render(
         ShapeProperties.create(matrix, clampedX, clampedY, handleSize, handleSize)
            .round(handleSize / 2.0F)
            .softness(1.0F)
            .thickness(2.0F)
            .color(16777215)
            .outlineColor(-1)
            .build()
      );
      if (this.hueDragging) {
         this.applyValue(mouseX, mouseY);
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.hueDragging = button == 0 && Calculate.isHovered(mouseX, mouseY, this.X, this.Y, this.W, this.H);
      if (this.hueDragging) {
         this.applyValue(mouseX, mouseY);
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.hueDragging = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.hueDragging && button == 0) {
         this.applyValue(mouseX, mouseY);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   private void applyValue(double mouseX, double mouseY) {
      this.setting.setBrightness(class_3532.method_15363(1.0F - (float)(mouseY - this.Y) / this.H, 0.0F, 1.0F));
      this.setting.setSaturation(class_3532.method_15363((float)(mouseX - this.X) / this.W, 0.0F, 1.0F));
   }

   public HueComponent(ColorSetting setting) {
      this.setting = setting;
   }
}
