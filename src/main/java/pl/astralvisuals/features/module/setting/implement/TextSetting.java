package pl.astralvisuals.features.module.setting.implement;

import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class TextSetting extends Setting {
   private String text;
   private int min;
   private int max;
   private String defaultText;

   public TextSetting(String name, String description) {
      super(name, description);
   }

   public TextSetting visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   @Override
   public void captureDefault() {
      this.defaultText = this.text;
   }

   @Override
   public void resetToDefault() {
      this.text = this.defaultText;
   }

   public String getText() {
      return this.text;
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public String getDefaultText() {
      return this.defaultText;
   }

   public TextSetting setText(String text) {
      this.text = text;
      return this;
   }

   public TextSetting setMin(int min) {
      this.min = min;
      return this;
   }

   public TextSetting setMax(int max) {
      this.max = max;
      return this;
   }

   public TextSetting setDefaultText(String defaultText) {
      this.defaultText = defaultText;
      return this;
   }
}
