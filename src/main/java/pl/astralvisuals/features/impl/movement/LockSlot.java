package pl.astralvisuals.features.impl.movement;

import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.utils.client.Instance;

// Блокирует ТОЛЬКО выброс предмета (Q) из выбранных слотов хотбара (перехват в ClientPlayerEntityMixin).
// Перемещение/клики по слотам в инвентаре не блокируются.
public class LockSlot extends Module {
   private final MultiSelectSetting slotsSetting = new MultiSelectSetting("Слоты", "Заблокированные слоты хотбара")
      .value("1", "2", "3", "4", "5", "6", "7", "8", "9")
      .selected("1");

   public static LockSlot getInstance() {
      return Instance.get(LockSlot.class);
   }

   public LockSlot() {
      super("LockSlot", "Lock Slot", ModuleCategory.PLAYER);
      this.setup(this.slotsSetting);
   }

   // hotbar: индекс слота хотбара 0..8.
   public boolean isSlotLocked(int hotbar) {
      return this.slotsSetting.isSelected(String.valueOf(hotbar + 1));
   }
}
