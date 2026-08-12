package pl.astralvisuals.features.module.setting.implement;

import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class BindSetting extends Setting {
   private int key = -1;
   private int type = 1;
   private int defaultKey = -1;
   private int defaultType = 1;

   public BindSetting(String name, String description) {
      super(name, description);
   }

   public BindSetting visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   @Override
   public void captureDefault() {
      this.defaultKey = this.key;
      this.defaultType = this.type;
   }

   @Override
   public void resetToDefault() {
      this.key = this.defaultKey;
      this.type = this.defaultType;
   }

   public int getKey() {
      return this.key;
   }

   public int getType() {
      return this.type;
   }

   public int getDefaultKey() {
      return this.defaultKey;
   }

   public int getDefaultType() {
      return this.defaultType;
   }

   public BindSetting setKey(int key) {
      this.key = key;
      return this;
   }

   public BindSetting setType(int type) {
      this.type = type;
      return this;
   }

   public BindSetting setDefaultKey(int defaultKey) {
      this.defaultKey = defaultKey;
      return this;
   }

   public BindSetting setDefaultType(int defaultType) {
      this.defaultType = defaultType;
      return this;
   }
}
