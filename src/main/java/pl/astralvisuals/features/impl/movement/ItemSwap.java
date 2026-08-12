package pl.astralvisuals.features.impl.movement;

import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2868;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.features.module.setting.implement.SelectSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

// Item Swap (как в Pulse): "Менять с" / "Менять на" + кнопка свапа.
// По нажатию переключает выбранный слот хотбара между предметом "с" и предметом "на".
public class ItemSwap extends Module {
   private static final String[] ITEMS = {"Тотем", "Жемчуг", "Кристалл", "Гапл", "Сфера", "Снежок"};

   private final SelectSetting fromSetting = new SelectSetting("Менять с", "Предмет, с которого свапаем").value(ITEMS).selected("Сфера");
   private final SelectSetting toSetting = new SelectSetting("Менять на", "Предмет, на который свапаем").value(ITEMS).selected("Тотем");
   private final BindSetting bindSetting = new BindSetting("Кнопка свапа", "Клавиша свапа");

   public static ItemSwap getInstance() {
      return Instance.get(ItemSwap.class);
   }

   public ItemSwap() {
      super("ItemSwap", "Item Swap", ModuleCategory.PLAYER);
      this.setup(this.fromSetting, this.toSetting, this.bindSetting);
   }

   private static class_1792 item(SelectSetting s) {
      if (s.isSelected("Тотем")) return class_1802.field_8288;
      if (s.isSelected("Жемчуг")) return class_1802.field_8634;
      if (s.isSelected("Кристалл")) return class_1802.field_8301;
      if (s.isSelected("Гапл")) return class_1802.field_8367;
      if (s.isSelected("Снежок")) return class_1802.field_8543;
      return class_1802.field_8543; // Сфера -> снежок (запасной маппинг)
   }

   // Ищет слот хотбара (0..8) с данным предметом.
   private int findHotbar(class_1792 target) {
      for (int i = 0; i < 9; i++) {
         if (mc.field_1724.method_31548().method_5438(i).method_7909() == target) {
            return i;
         }
      }

      return -1;
   }

   @EventHandler
   public void onKey(KeyEvent e) {
      if (mc.field_1724 == null || this.bindSetting.getKey() == -1 || !e.isKeyDown(this.bindSetting.getKey())) {
         return;
      }

      class_1792 from = item(this.fromSetting);
      class_1792 to = item(this.toSetting);
      class_1792 current = mc.field_1724.method_31548().method_5438(mc.field_1724.method_31548().field_7545).method_7909();

      int target;
      if (current == to) {
         target = this.findHotbar(from);
      } else {
         target = this.findHotbar(to);
      }

      if (target != -1) {
         mc.field_1724.method_31548().field_7545 = target;
         mc.field_1724.field_3944.method_52787(new class_2868(target));
      }
   }
}
