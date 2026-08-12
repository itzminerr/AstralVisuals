package pl.astralvisuals.display.hud;

import java.awt.Color;
import net.minecraft.class_10042;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_922;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.client.managers.api.draggable.AbstractDraggable;
import pl.astralvisuals.utils.client.target.TargetTracker;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.geometry.Render2D;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.display.style.GlassStyle;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;
import pl.astralvisuals.utils.math.calc.Calculate;
import pl.astralvisuals.utils.math.time.StopWatch;

public class TargetHud extends AbstractDraggable {
   private final Animation faceAlphaAnimation = new Decelerate().setMs(125).setValue(1.0);
   private final StopWatch stopWatch = new StopWatch();
   private final StopWatch distanceUpdateTimer = new StopWatch();
   private class_1309 lastTarget;
   private float health;
   private float absorption;
   private float displayedDistance;

   public TargetHud() {
      super("Таргет худ", 10, 80, 100, 36, true);
   }

   @Override
   public boolean visible() {
      return this.scaleAnimation.isDirection(Direction.FORWARDS);
   }

   @Override
   public void tick() {
      if (Interface.getInstance().state && Interface.getInstance().interfaceSettings.isSelected("Таргет худ")) {
         class_1309 target = TargetTracker.getTarget();
         if (target != null) {
            this.lastTarget = target;
            this.startAnimation();
            this.faceAlphaAnimation.setDirection(Direction.FORWARDS);
         } else if (PlayerInteractionHelper.isChat(mc.field_1755)) {
            this.lastTarget = mc.field_1724;
            this.startAnimation();
            this.faceAlphaAnimation.setDirection(Direction.FORWARDS);
         } else if (this.stopWatch.finished(500.0)) {
            this.stopAnimation();
            this.faceAlphaAnimation.setDirection(Direction.BACKWARDS);
         }
      } else {
         this.stopAnimation();
         this.faceAlphaAnimation.setDirection(Direction.BACKWARDS);
      }
   }

   @Override
   public void drawDraggable(class_332 context) {
      if (this.lastTarget != null) {
         class_4587 matrix = context.method_51448();
         this.drawMain(matrix);
         this.drawArmor(context, matrix);
         this.drawFace(context);
      }
   }

   private void drawMain(class_4587 matrix) {
      FontRenderer font = Fonts.getSize(18, Fonts.Type.REGULAR);
      FontRenderer distanceFont = Fonts.getSize(12, Fonts.Type.SEMI);
      float hp = PlayerInteractionHelper.getHealth(this.lastTarget);
      String stringHp = this.lastTarget.method_5767() ? " ??" : PlayerInteractionHelper.getHealthString(hp);
      this.health = class_3532.method_15363(Calculate.interpolateSmooth(1.0, this.health, hp / this.lastTarget.method_6063() * 360.0F), 0.0F, 360.0F);
      float absorptionAmount = this.lastTarget.method_6067();
      this.absorption = class_3532.method_15363(Calculate.interpolateSmooth(1.0, this.absorption, absorptionAmount / 20.0F * 360.0F), 0.0F, 360.0F);
      float actualDistance = mc.field_1724 != null ? mc.field_1724.method_5739(this.lastTarget) : 0.0F;
      float roundedDistance = Math.round(actualDistance * 2.0F) / 2.0F;
      if (this.distanceUpdateTimer.finished(10.0)) {
         this.displayedDistance = class_3532.method_15363(Calculate.interpolateSmooth(0.5, this.displayedDistance, roundedDistance), 0.0F, 100.0F);
         this.distanceUpdateTimer.reset();
      }

      String distanceText = String.format("%.1f", this.displayedDistance);
      float nameWidth = font.getStringWidth(this.lastTarget.method_5477().getString());
      float baseWidth = Math.max(80, 100);
      this.setWidth((int)baseWidth + 10);
      this.setHeight(41);
      GlassStyle.backdrop(matrix, this.getX(), this.getY(), this.getWidth(), this.getHeight() - 10, 12.0F);
      arc.render(
         ShapeProperties.create(matrix, this.getX() + this.getWidth() - 28.5F, this.getY() + 2.5F, 26.0, 26.0)
            .round(0.26F)
            .thickness(0.3F)
            .end(361.0F)
            .color(new Color(255, 255, 255, 25).getRGB())
            .build()
      );
      arc.render(
         ShapeProperties.create(matrix, this.getX() + this.getWidth() - 28.5F, this.getY() + 2.5F, 26.0, 26.0)
            .round(0.26F)
            .thickness(0.3F)
            .end(this.health)
            .color(ColorAssist.fade(0), ColorAssist.fade(200), ColorAssist.fade(0), ColorAssist.fade(200))
            .build()
      );
      if (this.absorption > 0.0F) {
         arc.render(
            ShapeProperties.create(matrix, this.getX() + this.getWidth() - 28.5F, this.getY() + 2.5F, 26.0, 26.0)
               .round(0.26F)
               .thickness(0.3F)
               .end(this.absorption - 2.5F)
               .color(
                  new Color(255, 215, 0, 255).getRGB(),
                  new Color(255, 128, 0, 255).getRGB(),
                  new Color(255, 215, 0, 255).getRGB(),
                  new Color(255, 128, 0, 255).getRGB()
               )
               .build()
         );
      }

      if (nameWidth > 50.0F) {
         ScissorAssist scissorManager = Force.getInstance().getScissorManager();
         scissorManager.push(matrix.method_23760().method_23761(), this.getX(), this.getY(), this.getWidth() - 29, this.getHeight());
         font.drawGradientString(
            matrix, this.lastTarget.method_5477().getString(), this.getX() + 29, this.getY() + 9.0F, ColorAssist.getText(), ColorAssist.getText(0.15F)
         );
         distanceFont.drawString(matrix, "Расстояние: " + distanceText, this.getX() + 29, this.getY() + 19.0F, new Color(225, 225, 255, 255).getRGB());
         scissorManager.pop();
      } else {
         font.drawString(matrix, this.lastTarget.method_5477().getString(), this.getX() + 29, this.getY() + 9.0F, ColorAssist.getText());
         distanceFont.drawString(matrix, "Расстояние: " + distanceText, this.getX() + 29, this.getY() + 19.0F, new Color(225, 225, 255, 255).getRGB());
      }

      float arcCenterX = this.getX() + this.getWidth() - 15.25F;
      float arcCenterY = this.getY() + 15.0F;
      Fonts.getSize(11, Fonts.Type.BOLD).drawCenteredString(matrix, stringHp, arcCenterX, arcCenterY, new Color(255, 255, 255, 225).getRGB());
   }

   private void drawArmor(class_332 context, class_4587 matrix) {
      class_1799[] slots = new class_1799[]{
         this.lastTarget.method_6047(),
         this.lastTarget.method_6079(),
         this.lastTarget.method_6118(class_1304.field_6169),
         this.lastTarget.method_6118(class_1304.field_6174),
         this.lastTarget.method_6118(class_1304.field_6172),
         this.lastTarget.method_6118(class_1304.field_6166)
      };
      float x = this.getX() + 21.0F;
      float y = this.getY() + 17;
      float slotSize = 10.0F;

      for (int i = 0; i < 6; i++) {
         float currentX = x + i * 11.5F;
         float currentY = y + 15.0F;
         GlassStyle.backdrop(matrix, currentX - 0.5F, currentY, slotSize + 1.0F, slotSize + 1.0F, 3.0F);
         if (!slots[i].method_7960()) {
            Render2D.defaultDrawStack(context, slots[i], currentX, currentY + 0.5F, false, false, 0.5F);
         } else {
            String xText = "x";
            FontRenderer font = Fonts.getSize(12, Fonts.Type.DEFAULT);
            float textWidth = font.getStringWidth(xText);
            float textHeight = font.getStringHeight(xText);
            float textX = currentX + (slotSize - textWidth) / 2.0F;
            float textY = currentY + 0.5F + (slotSize - textHeight) / 2.0F;
            font.drawString(matrix, xText, textX, textY + 6.25F, new Color(225, 225, 255, 255).getRGB());
         }
      }
   }

   private void drawFace(class_332 context) {
      if (mc.method_1561().method_3953(this.lastTarget) instanceof class_922 renderer) {
         class_10042 state = (class_10042)renderer.method_62425(this.lastTarget, tickCounter.method_60637(false));
         class_2960 textureLocation = renderer.method_3885(state);
         float alpha = this.faceAlphaAnimation.getOutput().floatValue();
         Calculate.setAlpha(
            alpha,
            () -> Render2D.drawTexture(
               context,
               textureLocation,
               this.getX() + 5,
               this.getY() + 5.5F,
               20.0F,
               4.0F,
               8,
               8,
               64,
               ColorAssist.getRect(1.0F),
               ColorAssist.multRed(-1, 1.0F + this.lastTarget.field_6235 / 4.0F)
            )
         );
      }
   }
}
