package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class CheckComponent extends AbstractComponent {
   private static final float TRACK_W = 18.0F;
   private static final float TRACK_H = 9.0F;
   private static final float KNOB_D = 7.0F;
   private boolean state;
   private Runnable runnable;
   private final Animation fillAnim = new Decelerate().setMs(220).setValue(100.0);

   public CheckComponent position(float x, float y) {
      this.x = x;
      this.y = y;
      return this;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.fillAnim.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      float fill = class_3532.method_15363(this.fillAnim.getOutput().floatValue() / 100.0F, 0.0F, 1.0F);
      float knobX = this.x + 1.0F + 9.0F * fill;
      float knobY = this.y + 1.0F;
      rectangle.render(ShapeProperties.create(matrix, this.x, this.y, 18.0, 9.0).round(4.5F).color(new Color(40, 40, 42, 165).getRGB()).build());
      int fillA = class_3532.method_15340((int)(fill * 255.0F), 0, 255);
      rectangle.render(ShapeProperties.create(matrix, this.x, this.y, 18.0, 9.0).round(4.5F).color(new Color(20, 20, 20, fillA).getRGB()).build());
      rectangle.render(
         ShapeProperties.create(matrix, this.x, this.y, 18.0, 9.0)
            .round(4.5F)
            .thickness(0.6F)
            .outlineColor(new Color(65, 65, 68, 180).getRGB())
            .color(0)
            .build()
      );
      rectangle.render(ShapeProperties.create(matrix, knobX, knobY, 7.0, 7.0).round(3.5F).color(new Color(238, 238, 242, 255).getRGB()).build());
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0 && Calculate.isHovered(mouseX, mouseY, this.x, this.y, 18.0, 9.0)) {
         if (!this.state) {
            SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
         } else {
            SoundManager.playSound(SoundManager.DISABLE_MODULE, 1.0F, 1.0F);
         }

         this.runnable.run();
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public CheckComponent setState(boolean state) {
      this.state = state;
      return this;
   }

   public CheckComponent setRunnable(Runnable runnable) {
      this.runnable = runnable;
      return this;
   }
}
