package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ButtonComponent extends AbstractComponent {
   private String text;
   private Runnable runnable;
   private int color = -13882320;

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.width = Fonts.getSize(12).getStringWidth(this.text) + 13.0F;
      this.height = 12.0F;
      rectangle.render(ShapeProperties.create(matrix, this.x, this.y, this.width, this.height).round(2.0F).color(this.color).build());
      Fonts.getSize(12, Fonts.Type.BOLD).drawCenteredString(matrix, this.text, this.x + this.width / 2.0, this.y + 5.0F, -1);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
         this.runnable.run();
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public ButtonComponent setText(String text) {
      this.text = text;
      return this;
   }

   public ButtonComponent setRunnable(Runnable runnable) {
      this.runnable = runnable;
      return this;
   }

   public ButtonComponent setColor(int color) {
      this.color = color;
      return this;
   }
}
