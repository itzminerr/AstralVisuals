package pl.astralvisuals.display.screens.clickgui.components.implement.other;

import java.awt.Color;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.utils.client.sound.SoundManager;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.math.calc.Calculate;

public class ToggleComponent extends AbstractComponent {
   private boolean state;
   private Runnable runnable;
   private float toggleWidth = 24.0F;
   private float toggleHeight = 12.0F;
   private final Animation slideAnimation = new Decelerate().setMs(200).setValue(this.toggleHeight - 4.0F);

   public ToggleComponent setSize(float width, float height) {
      this.toggleWidth = width;
      this.toggleHeight = height;
      this.slideAnimation.setValue(this.getSlideDistance());
      return this;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.slideAnimation.setDirection(this.state ? Direction.FORWARDS : Direction.BACKWARDS);
      float slideOffset = this.slideAnimation.getOutput().floatValue();
      float round = this.toggleHeight / 2.0F;
      if (this.state) {
         // ВКЛ: неоновый акцент-градиент из Client Color + мягкое свечение.
         int accentA = GlassStyle.accentStart();
         int accentB = GlassStyle.accentEnd();
         GlassStyle.glow(matrix, this.x, this.y, this.toggleWidth, this.toggleHeight, round, GlassStyle.accentMid(), 0.22F);
         rectangle.render(
            ShapeProperties.create(matrix, this.x, this.y, this.toggleWidth, this.toggleHeight)
               .round(round)
               .color(accentA, accentA, accentB, accentB)
               .build()
         );
      } else {
         // ВЫКЛ: угловатый кибер-трек — глубокий чёрный с лёгким синим отливом.
         rectangle.render(
            ShapeProperties.create(matrix, this.x, this.y, this.toggleWidth, this.toggleHeight)
               .round(round)
               .color(new Color(8, 9, 14, 220).getRGB())
               .build()
         );
      }
      // Тонкая неоновая рамка трека: тусклый циан (ВЫКЛ) или яркий акцент (ВКЛ задаётся выше через glow).
      int trackBorderColor = this.state ? GlassStyle.accentStart() : new Color(0, 200, 230, 110).getRGB();
      rectangle.render(
         ShapeProperties.create(matrix, this.x, this.y, this.toggleWidth, this.toggleHeight)
            .round(this.toggleHeight / 2.0F)
            .thickness(0.9F)
            .outlineColor(trackBorderColor)
            .color(0)
            .build()
      );
      float circleSize = this.toggleHeight - 4.0F;
      float circleX = this.x + 2.0F + Math.min(slideOffset, this.getSlideDistance());
      float circleY = this.y + (this.toggleHeight - circleSize) / 2.0F;
      // Ползунок: неоновое свечение + заливка акцентом (ВКЛ) или белый (ВЫКЛ).
      if (this.state) {
         GlassStyle.glow(matrix, circleX, circleY, circleSize, circleSize, circleSize / 2.0F, GlassStyle.accentStart(), 0.5F);
         rectangle.render(
            ShapeProperties.create(matrix, circleX, circleY, circleSize, circleSize)
               .round(circleSize / 2.0F)
               .color(new Color(255, 255, 255, 255).getRGB())
               .build()
         );
      } else {
         rectangle.render(
            ShapeProperties.create(matrix, circleX, circleY, circleSize, circleSize)
               .round(circleSize / 2.0F)
               .color(new Color(170, 180, 200, 255).getRGB())
               .build()
         );
      }
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.toggleWidth, this.toggleHeight) && button == 0) {
         if (!this.state) {
            SoundManager.playSound(SoundManager.ENABLE_MODULE, 1.0F, 1.0F);
         } else {
            SoundManager.playSound(SoundManager.DISABLE_MODULE, 1.0F, 1.0F);
         }

         if (this.runnable != null) {
            this.runnable.run();
         }

         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private float getSlideDistance() {
      return Math.max(0.0F, this.toggleWidth - this.toggleHeight);
   }

   public ToggleComponent setState(boolean state) {
      this.state = state;
      return this;
   }

   public ToggleComponent setRunnable(Runnable runnable) {
      this.runnable = runnable;
      return this;
   }

   public ToggleComponent setToggleWidth(float toggleWidth) {
      this.toggleWidth = toggleWidth;
      return this;
   }

   public ToggleComponent setToggleHeight(float toggleHeight) {
      this.toggleHeight = toggleHeight;
      return this;
   }
}
