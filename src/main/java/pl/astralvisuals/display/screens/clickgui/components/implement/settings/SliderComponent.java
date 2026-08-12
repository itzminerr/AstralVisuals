package pl.astralvisuals.display.screens.clickgui.components.implement.settings;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.display.scissor.ScissorAssist;
import pl.astralvisuals.utils.display.shape.ShapeProperties;
import pl.astralvisuals.utils.math.calc.Calculate;

public class SliderComponent extends AbstractSettingComponent {
   public static final int SLIDER_WIDTH = 110;
   private static final int SLIDER_OFFSET_X = 9;
   private static final float HANDLE_SIZE = 9.0F;
   private static final float INNER_HANDLE_SIZE = 7.0F;
   private static final float TRACK_HEIGHT = 5.0F;
   private final SliderSettings setting;
   private boolean dragging;
   private double animation;

   public SliderComponent(SliderSettings setting) {
      super(setting);
      this.setting = setting;
   }

   @Override
   public void render(class_332 context, int mouseX, int mouseY, float delta) {
      class_4587 matrix = context.method_51448();
      this.height = 30.0F;
      this.changeValue(this.getDifference(mouseX));
      float availableNameWidth = this.width - 18.0F - Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(this.formatValue(this.setting.getValue())) - 8.0F;
      float nameWidth = Fonts.getSize(12, Fonts.Type.DEFAULT).getStringWidth(this.setting.getName());
      String value = this.formatValue(this.setting.getValue());
      Fonts.getSize(12, Fonts.Type.BOLD)
         .drawString(matrix, value, this.x + this.width - 9.0F - Fonts.getSize(12, Fonts.Type.BOLD).getStringWidth(value), this.y + 8.0F, ColorAssist.getText());
      if (nameWidth > availableNameWidth) {
         ScissorAssist scissor = Force.getInstance().getScissorManager();
         scissor.push(matrix.method_23760().method_23761(), this.x + 9.0F, this.y + 4.0F, availableNameWidth, 14.0F);
         Fonts.getSize(12, Fonts.Type.DEFAULT)
            .drawStringWithScroll(matrix, this.setting.getName(), this.x + 9.0F, this.y + 8.0F, availableNameWidth, new Color(225, 225, 225, 225).getRGB());
         scissor.pop();
      } else {
         Fonts.getSize(12, Fonts.Type.DEFAULT).drawString(matrix, this.setting.getName(), this.x + 9.0F, this.y + 8.0F, -2828575);
      }

      this.renderSlider(matrix, mouseX);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 1 && this.isComponentHovered(mouseX, mouseY)) {
         this.dragging = false;
         this.setting.resetValue();
         return true;
      } else if (button == 0 && this.isComponentHovered(mouseX, mouseY)) {
         this.dragging = true;
         this.changeValue(this.getDifference(mouseX));
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragging = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.dragging && button == 0) {
         this.changeValue(this.getDifference(mouseX));
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   @Override
   public boolean isHover(double mouseX, double mouseY) {
      return this.isComponentHovered(mouseX, mouseY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (amount != amount && this.isComponentHovered(mouseX, mouseY)) {
         float step = this.getStep();
         if (class_310.method_1551().field_1690.field_1832.method_1434()) {
            step *= 10.0F;
         }

         float value = this.setting.getValue() + (amount > 0.0 ? step : -step);
         this.setting.setValue(this.snapValue(value));
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, amount);
      }
   }

   private void renderSlider(class_4587 matrix, double mouseX) {
      float sliderX = this.x + 9.0F;
      float sliderW = this.width - 18.0F;
      float sliderY = this.y + this.height - 5.0F - 5.0F;
      float percentValue = sliderW * (this.setting.getValue() - this.setting.getMin()) / (this.setting.getMax() - this.setting.getMin());
      if (this.dragging) {
         this.animation = class_3532.method_15350(mouseX - sliderX, 0.0, sliderW);
      } else {
         this.animation = Calculate.interpolate(this.animation, (double)percentValue);
      }

      rectangle.render(ShapeProperties.create(matrix, sliderX, sliderY, sliderW, 5.0).round(2.5F).color(new Color(40, 40, 45, 180).getRGB()).build());
      rectangle.render(
         ShapeProperties.create(matrix, sliderX, sliderY, (float)this.animation, 5.0)
            .round(2.5F)
            .color(
               new Color(55, 55, 60, 200).getRGB(),
               new Color(155, 155, 160, 200).getRGB(),
               new Color(255, 255, 255, 200).getRGB(),
               new Color(255, 255, 250, 200).getRGB()
            )
            .build()
      );
      float handleX = sliderX + class_3532.method_15363((float)this.animation - 4.5F, 0.0F, sliderW - 9.0F);
      float handleY = sliderY + -2.0F;
      rectangle.render(ShapeProperties.create(matrix, handleX, handleY, 9.0, 9.0).round(4.5F).color(ColorAssist.getMainGuiColor()).build());
      rectangle.render(
         ShapeProperties.create(matrix, handleX + 1.0F, handleY + 1.0F, 7.0, 7.0)
            .round(3.5F)
            .thickness(2.0F)
            .softness(0.0F)
            .color(
               new Color(61, 67, 71, 255).getRGB(),
               new Color(71, 77, 81, 255).getRGB(),
               new Color(81, 87, 91, 255).getRGB(),
               new Color(91, 97, 101, 255).getRGB()
            )
            .build()
      );
   }

   private float getTrackX() {
      return this.x + 9.0F;
   }

   private float getTrackWidth() {
      return this.width - 18.0F;
   }

   private double getDifference(double mouseX) {
      return class_3532.method_15350(mouseX - this.getTrackX(), 0.0, this.getTrackWidth());
   }

   private void changeValue(double difference) {
      if (this.dragging) {
         float raw = (float)(difference / this.getTrackWidth() * (this.setting.getMax() - this.setting.getMin()) + this.setting.getMin());
         this.setting.setValue(this.snapToStep(raw));
      }
   }

   private float snapToStep(float value) {
      float clamped = class_3532.method_15363(value, this.setting.getMin(), this.setting.getMax());
      if (this.setting.isInteger()) {
         return Math.round(clamped);
      } else {
         float step = this.getStep();
         float snapped = Math.round((clamped - this.setting.getMin()) / step) * step + this.setting.getMin();
         return BigDecimal.valueOf((double)snapped).setScale(this.resolveScale(), RoundingMode.HALF_UP).floatValue();
      }
   }

   private float snapValue(float value) {
      return this.snapToStep(value);
   }

   private int resolveScale() {
      float range = this.setting.getMax() - this.setting.getMin();
      if (range <= 1.0F) {
         return 3;
      } else {
         return range <= 100.0F ? 2 : 1;
      }
   }

   private float getStep() {
      if (this.setting.isInteger()) {
         return 1.0F;
      } else {
         float range = this.setting.getMax() - this.setting.getMin();
         float trackW = this.width > 0.0F ? this.getTrackWidth() : 110.0F;
         float pixelStep = range / (trackW * 2.0F);
         if (range <= 1.0F) {
            return Math.max(0.001F, this.roundToNiceStep(pixelStep));
         } else if (range <= 10.0F) {
            return Math.max(0.01F, this.roundToNiceStep(pixelStep));
         } else {
            return range <= 100.0F ? Math.max(0.01F, this.roundToNiceStep(pixelStep)) : Math.max(0.1F, this.roundToNiceStep(pixelStep));
         }
      }
   }

   private float roundToNiceStep(float step) {
      if (step <= 0.0F) {
         return 0.01F;
      } else {
         double magnitude = Math.pow(10.0, Math.floor(Math.log10(step)));
         double normalized = step / magnitude;
         double nice;
         if (normalized <= 1.0) {
            nice = 1.0;
         } else if (normalized <= 2.0) {
            nice = 2.0;
         } else if (normalized <= 5.0) {
            nice = 5.0;
         } else {
            nice = 10.0;
         }

         return (float)(nice * magnitude);
      }
   }

   private String formatValue(float value) {
      if (this.setting.isInteger()) {
         return String.valueOf(Math.round(value));
      } else {
         String formatted = BigDecimal.valueOf((double)value).setScale(this.resolveScale(), RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
         return formatted.contains(".") ? formatted : formatted + ".0";
      }
   }

   private boolean isSliderHovered(double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, this.getTrackX(), this.y + this.height - 5.0F - 9.0F, this.getTrackWidth(), 13.0);
   }

   private boolean isComponentHovered(double mouseX, double mouseY) {
      return Calculate.isHovered(mouseX, mouseY, this.x + 4.0F, this.y + 4.0F, this.width - 8.0F, this.height - 2.0F);
   }
}
