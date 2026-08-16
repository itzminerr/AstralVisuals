package pl.astralvisuals.features.impl.movement;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.features.module.setting.implement.SliderSettings;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.display.color.ColorAssist;

public final class ItemHighlighter extends Module {
   private final BooleanSetting pulse = new BooleanSetting("Пульсация", "Плавно изменять яркость подсветки").setValue(false);
   private final SliderSettings blinkSpeed = new SliderSettings("Скорость", "Скорость пульсации").setValue(6.0F).range(1.0F, 20.0F)
      .visible(this.pulse::isValue);
   private final SliderSettings opacity = new SliderSettings("Прозрачность", "Прозрачность подсветки").setValue(0.35F).range(0.05F, 1.0F);
   private final Map<class_1792, Entry> entries = new LinkedHashMap<>();

   public static ItemHighlighter getInstance() {
      return Instance.get(ItemHighlighter.class);
   }

   public ItemHighlighter() {
      super("ItemHighlighter", "Item Highlighter", ModuleCategory.RENDER);
      this.add(class_1802.field_8449, "Дезориентация", new Color(165, 92, 255));
      this.add(class_1802.field_8814, "Огненный смерч", new Color(255, 120, 40));
      this.add(class_1802.field_8479, "Явная пыль", new Color(235, 235, 235));
      this.add(class_1802.field_8288, "Тотем бессмертия", new Color(90, 220, 120));
      this.add(class_1802.field_8287, "Пузырёк опыта", new Color(0, 206, 255));
      this.add(class_1802.field_22021, "Трапка", new Color(130, 130, 130));
      this.add(class_1802.field_8233, "Хорус", new Color(190, 120, 255));
      this.add(class_1802.field_8551, "Пласт", new Color(110, 185, 70));
      this.add(class_1802.field_8463, "Гепл", new Color(245, 197, 66));
      this.add(class_1802.field_8367, "Чарка", new Color(210, 120, 255));
      this.add(class_1802.field_8634, "Перка", new Color(60, 210, 200));
      this.add(class_1802.field_8543, "Снежок Заморозка", new Color(160, 220, 255));

      var settings = new java.util.ArrayList<pl.astralvisuals.features.module.setting.Setting>();
      settings.add(this.pulse);
      settings.add(this.blinkSpeed);
      settings.add(this.opacity);
      this.entries.values().forEach(entry -> {
         settings.add(entry.enabled());
         settings.add(entry.color());
      });
      this.setup(settings.toArray(pl.astralvisuals.features.module.setting.Setting[]::new));
   }

   private void add(class_1792 item, String name, Color defaultColor) {
      BooleanSetting enabled = new BooleanSetting(name, "Подсвечивать этот предмет").setValue(true);
      ColorSetting color = new ColorSetting("Цвет " + name, "Цвет подсветки").setColor(defaultColor.getRGB()).visible(enabled::isValue);
      this.entries.put(item, new Entry(enabled, color));
   }

   public void renderSlot(class_332 context, class_1735 slot) {
      if (!this.isState() || slot == null || !slot.method_7681() || !(slot.field_7871 instanceof class_1661)) {
         return;
      }
      this.draw(context, slot.field_7873 - 1, slot.field_7872 - 1, slot.method_7677());
   }

   public void renderHotbar(class_332 context, int x, int y, class_1799 stack) {
      if (this.isState()) {
         this.draw(context, x - 1, y - 1, stack);
      }
   }

   private void draw(class_332 context, int x, int y, class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return;
      }
      Entry entry = this.entries.get(stack.method_7909());
      if (entry == null || !entry.enabled().isValue()) {
         return;
      }
      float alpha = this.opacity.getValue();
      if (this.pulse.isValue()) {
         float phase = (float)(System.currentTimeMillis() / 1000.0 * this.blinkSpeed.getValue() * Math.PI * 2.0);
         alpha *= 0.5F + 0.5F * class_3532.method_15374(phase);
      }
      int color = ColorAssist.replAlpha(entry.color().getColor(), class_3532.method_15363(alpha, 0.0F, 1.0F));
      context.method_25294(x, y, x + 18, y + 18, color);
   }

   private record Entry(BooleanSetting enabled, ColorSetting color) {
   }
}
