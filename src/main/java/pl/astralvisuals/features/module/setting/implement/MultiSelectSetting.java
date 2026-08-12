package pl.astralvisuals.features.module.setting.implement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import pl.astralvisuals.features.module.setting.Setting;

public class MultiSelectSetting extends Setting {
   private List<String> list;
   private List<String> selected = new ArrayList<>();
   private List<String> defaultSelected = new ArrayList<>();

   public MultiSelectSetting(String name, String description) {
      super(name, description);
   }

   public MultiSelectSetting value(String... settings) {
      this.list = Arrays.asList(settings);
      return this;
   }

   public MultiSelectSetting selected(String... settings) {
      this.selected = new ArrayList<>(Arrays.asList(settings));
      this.defaultSelected = new ArrayList<>(this.selected);
      return this;
   }

   public MultiSelectSetting visible(Supplier<Boolean> visible) {
      this.setVisible(visible);
      return this;
   }

   public boolean isSelected(String name) {
      return this.selected.contains(name);
   }

   @Override
   public void captureDefault() {
      this.defaultSelected = new ArrayList<>(this.selected);
   }

   @Override
   public void resetToDefault() {
      this.selected = new ArrayList<>(this.defaultSelected);
   }

   public List<String> getList() {
      return this.list;
   }

   public List<String> getSelected() {
      return this.selected;
   }

   public List<String> getDefaultSelected() {
      return this.defaultSelected;
   }

   public void setList(List<String> list) {
      this.list = list;
   }

   public void setSelected(List<String> selected) {
      this.selected = selected;
   }

   public void setDefaultSelected(List<String> defaultSelected) {
      this.defaultSelected = defaultSelected;
   }
}
