package pl.astralvisuals.features.module.setting.implement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class SelectSetting extends Setting {
   private String selected;
   private List<String> list;
   private String defaultSelected = "";

   public SelectSetting(String name, String description) {
      super(name, description);
   }

   public SelectSetting value(String... values) {
      this.list = Arrays.asList(values);
      this.selected = this.list.isEmpty() ? "" : this.list.get(0);
      this.defaultSelected = this.selected;
      return this;
   }

   public SelectSetting visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   public SelectSetting selected(String string) {
      if (this.list.contains(string)) {
         this.selected = string;
         this.defaultSelected = string;
      }

      return this;
   }

   public boolean isSelected(String name) {
      return this.selected.equals(name);
   }

   @Override
   public void captureDefault() {
      this.defaultSelected = this.selected;
   }

   @Override
   public void resetToDefault() {
      this.selected = this.defaultSelected;
   }

   public String getSelected() {
      return this.selected;
   }

   public List<String> getList() {
      return this.list;
   }

   public String getDefaultSelected() {
      return this.defaultSelected;
   }

   public void setSelected(String selected) {
      this.selected = selected;
   }
}
