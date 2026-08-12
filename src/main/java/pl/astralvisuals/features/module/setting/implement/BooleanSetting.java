package pl.astralvisuals.features.module.setting.implement;

import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class BooleanSetting extends Setting {
   private boolean value;
   private int key = -1;
   private int type = 1;
   private boolean defaultValue;
   private int defaultKey = -1;
   private int defaultType = 1;

   public BooleanSetting(String name, String description) {
      super(name, description);
   }

   public BooleanSetting visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   @Override
   public void captureDefault() {
      this.defaultValue = this.value;
      this.defaultKey = this.key;
      this.defaultType = this.type;
   }

   @Override
   public void resetToDefault() {
      this.value = this.defaultValue;
      this.key = this.defaultKey;
      this.type = this.defaultType;
   }

   public boolean isValue() {
      return this.value;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public boolean isDefaultValue() {
      return this.defaultValue;
   }

   public int getDefaultKey() {
      return this.defaultKey;
   }

   public int getDefaultType() {
      return this.defaultType;
   }

   public BooleanSetting setValue(boolean value) {
      this.value = value;
      return this;
   }

   public BooleanSetting setKey(int key) {
      this.key = key;
      return this;
   }

   public BooleanSetting setType(int type) {
      this.type = type;
      return this;
   }

   public BooleanSetting setDefaultValue(boolean defaultValue) {
      this.defaultValue = defaultValue;
      return this;
   }

   public BooleanSetting setDefaultKey(int defaultKey) {
      this.defaultKey = defaultKey;
      return this;
   }

   public BooleanSetting setDefaultType(int defaultType) {
      this.defaultType = defaultType;
      return this;
   }
}
