package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class StatusRender extends AbstractComponent {
   private static final float TRACK_W = 24.0F;
   private static final float TRACK_H = 13.0F;
   private static final float KNOB_D = 15.0F;
   private static final float KNOB_OFF = -1.0F;
   private static final float KNOB_ON = 10.0F;
   private boolean state;
   private Runnable runnable;
   private final Animation slideAnim = new Decelerate().setMs(220).setValue(10.0);
   private final Animation fillAnim = new Decelerate().setMs(300).setValue(100.0);

   public StatusRender() {
      this.slideAnim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      this.fillAnim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      this.slideAnim.reset();
      this.fillAnim.reset();
   }

   public StatusRender position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.slideAnim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      this.fillAnim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      float fill = class_3532.method_15363(this.fillAnim.getOutput().floatValue() / 100.0F, 0.0F, 1.0F);
      float knobX = this.x + class_3532.method_15363(-1.0F + 11.0F * fill, -1.0F, 10.0F);
      rectangle.render(ShapeProperties.create(matrix, this.x, this.y, 24.0, 13.0).round(6.5F).color(new Color(30, 30, 38, 255).getRGB()).build());
      int fillAlpha = class_3532.method_15340((int)(fill * 180.0F), 0, 255);
      rectangle.render(ShapeProperties.create(matrix, this.x, this.y, 24.0, 13.0).round(6.5F).color(new Color(80, 80, 120, fillAlpha).getRGB()).build());
      rectangle.render(
         ShapeProperties.create(matrix, this.x, this.y, 24.0, 13.0)
            .round(6.5F)
            .thickness(0.7F)
            .outlineColor(new Color(65, 65, 85, 200).getRGB())
            .color(0)
            .build()
      );
      rectangle.render(ShapeProperties.create(matrix, knobX, this.y - 1.0F, 15.0, 15.0).round(7.5F).color(new Color(225, 225, 235, 255).getRGB()).build());
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && Calculate.isHovered(mouseX, mouseY, this.x, this.y, 24.0, 13.0)) {
         this.state = !this.state;
         this.runnable.run();
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public StatusRender setState(boolean state) {
      this.state = state;
      return this;
   }

   public StatusRender setRunnable(Runnable runnable) {
      this.runnable = runnable;
      return this;
   }
}
