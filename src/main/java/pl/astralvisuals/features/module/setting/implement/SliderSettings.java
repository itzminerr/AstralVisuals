package pl.astralvisuals.features.module.setting.implement;

import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class SliderSettings extends Setting {
   private float value;
   private float min;
   private float max;
   private boolean integer;
   private float defaultValue;
   private boolean defaultValueInitialized;

   public SliderSettings(String name, String description) {
      super(name, description);
   }

   public SliderSettings range(float min, float max) {
      this.min = min;
      this.max = max;
      return this;
   }

   public SliderSettings range(int min, int max) {
      this.min = min;
      this.max = max;
      this.integer = true;
      return this;
   }

   public int getInt() {
      return (int)this.value;
   }

   public SliderSettings setValue(float value) {
      this.value = value;
      if (!this.defaultValueInitialized) {
         this.defaultValue = value;
         this.defaultValueInitialized = true;
      }

      return this;
   }

   public SliderSettings resetValue() {
      this.value = this.defaultValueInitialized ? this.defaultValue : this.min;
      return this;
   }

   @Override
   public void captureDefault() {
      this.defaultValue = this.value;
      this.defaultValueInitialized = true;
   }

   @Override
   public void resetToDefault() {
      this.resetValue();
   }

   public SliderSettings visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   public float getValue() {
      return this.value;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public boolean isInteger() {
      return this.integer;
   }

   public float getDefaultValue() {
      return this.defaultValue;
   }

   public boolean isDefaultValueInitialized() {
      return this.defaultValueInitialized;
   }

   public SliderSettings setMin(float min) {
      this.min = min;
      return this;
   }

   public SliderSettings setMax(float max) {
      this.max = max;
      return this;
   }

   public SliderSettings setInteger(boolean integer) {
      this.integer = integer;
      return this;
   }

   public SliderSettings setDefaultValue(float defaultValue) {
      this.defaultValue = defaultValue;
      return this;
   }

   public SliderSettings setDefaultValueInitialized(boolean defaultValueInitialized) {
      this.defaultValueInitialized = defaultValueInitialized;
      return this;
   }
}
