package pl.astralvisuals.display.screens.clickgui.components.implement.settings.select;

import java.awt.Color;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import org.joml.Vector4f;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class SelectedButton extends AbstractComponent {
   private final SelectSetting setting;
   private final String text;
   private float alpha;
   private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(0.5);

   public SelectedButton(SelectSetting setting, String text) {
      this.setting = setting;
      this.text = text;
      this.alphaAnimation.setDirection(Direction.BACKWARDS);
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.alphaAnimation.setDirection(this.setting.isSelected(this.text) ? Direction.FORWARDS : Direction.BACKWARDS);
      float opacity = this.alphaAnimation.getOutput().floatValue();
      int adjustedAlpha = (int)Calculate.clamp(opacity * this.alpha * 255.0F, 0.0F, 255.0F);
      if (!this.alphaAnimation.isFinished(Direction.BACKWARDS)) {
         rectangle.render(
            ShapeProperties.create(context.method_51448(), this.x + 0.5F, this.y, this.width - 1.0F, this.height - 0.5F)
               .round(getRound(this.setting.getList(), this.text))
               .color(
                  new Color(58, 58, 60, adjustedAlpha).getRGB(),
                  new Color(58, 58, 60, adjustedAlpha).getRGB(),
                  new Color(58, 58, 60, 0).getRGB(),
                  new Color(58, 58, 60, 0).getRGB()
               )
               .build()
         );
      }

      Fonts.getSize(12, Fonts.Type.BOLD)
         .drawString(
            matrix,
            this.text,
            this.x + 4.0F,
            this.y + 5.0F,
            ColorAssist.multAlpha(new Color(225, 225, 225, 225).getRGB(), Calculate.clamp(this.alpha, 0.0F, 1.0F))
         );
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (Calculate.isHovered(mouseX, mouseY, this.x, this.y, this.width, this.height) && button == 0) {
         this.setting.setSelected(this.text);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public static Vector4f getRound(List<String> list, String text) {
      if (list.size() == 1) {
         return new Vector4f(3.0F);
      } else if (list.get(list.size() - 1).equals(text)) {
         return new Vector4f(0.0F, 3.0F, 0.0F, 3.0F);
      } else {
         return list.get(0).equals(text) ? new Vector4f(3.0F, 0.0F, 3.0F, 0.0F) : new Vector4f(0.0F);
      }
   }

   public SelectedButton setAlpha(float alpha) {
      this.alpha = alpha;
      return this;
   }
}
