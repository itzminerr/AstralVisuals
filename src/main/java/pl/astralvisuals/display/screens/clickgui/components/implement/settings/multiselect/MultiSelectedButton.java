package pl.astralvisuals.display.screens.clickgui.components.implement.settings.multiselect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import pl.astralvisuals.common.animation.Animation;
import pl.astralvisuals.common.animation.Direction;
import pl.astralvisuals.common.animation.implement.Decelerate;
import pl.astralvisuals.display.screens.clickgui.components.AbstractComponent;
import pl.astralvisuals.display.screens.clickgui.components.implement.settings.select.SelectedButton;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class MultiSelectedButton extends AbstractComponent {
   private final MultiSelectSetting setting;
   private final String text;
   private float alpha;
   private final Animation alphaAnimation = new Decelerate().setMs(300).setValue(0.5);

   public MultiSelectedButton(MultiSelectSetting setting, String text) {
      this.setting = setting;
      this.text = text;
      this.alphaAnimation.setDirection(Direction.BACKWARDS);
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.alphaAnimation.setDirection(this.setting.getSelected().contains(this.text) ? Direction.FORWARDS : Direction.BACKWARDS);
      float opacity = this.alphaAnimation.getOutput().floatValue();
      int adjustedAlpha = (int)Calculate.clamp(opacity * this.alpha * 255.0F, 0.0F, 255.0F);
      int selectedOpacity = new Color(48, 48, 51, adjustedAlpha).getRGB();
      if (!this.alphaAnimation.isFinished(Direction.BACKWARDS)) {
         rectangle.render(
            ShapeProperties.create(context.method_51448(), this.x + 0.5F, this.y, this.width - 1.0F, this.height - 0.5F)
               .round(SelectedButton.getRound(this.setting.getList(), this.text))
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
         List<String> selected = new ArrayList<>(this.setting.getSelected());
         if (selected.contains(this.text)) {
            selected.remove(this.text);
         } else {
            selected.add(this.text);
            this.sortSelectedAccordingToList(selected, this.setting.getList());
         }

         this.setting.setSelected(selected);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void sortSelectedAccordingToList(List<String> selected, List<String> list) {
      selected.sort(Comparator.comparingInt(list::indexOf));
   }

   public MultiSelectedButton setAlpha(float alpha) {
      this.alpha = alpha;
      return this;
   }
}
