package pl.astralvisuals.utils.interactions.inv;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public class InventoryTask implements QuickImports {
   public static void moveItem(class_1735 from, int to, boolean task, boolean updateInventory) {
      if (from != null) {
         moveItem(from.field_7874, to);
      }
   }

   public static void moveItem(int from, int to) {
      if (!PlayerInteractionHelper.nullCheck() && from != to && from >= 0 && to >= 0) {
         int syncId = mc.field_1724.field_7512.field_7763;
         clickSlot(syncId, from, 0, class_1713.field_7790);
         clickSlot(syncId, to, 0, class_1713.field_7790);
         clickSlot(syncId, from, 0, class_1713.field_7790);
      }
   }

   public static void swapHand(class_1735 slot, class_1268 hand, boolean task, boolean updateInventory) {
      if (slot != null && !PlayerInteractionHelper.nullCheck()) {
         int targetSlot = hand == class_1268.field_5810 ? 45 : 36 + mc.field_1724.method_31548().field_7545;
         moveItem(slot.field_7874, targetSlot);
      }
   }

   public static class_1735 getSlot(class_1792 item, Comparator<class_1735> comparator, Predicate<class_1735> filter) {
      return slots().filter(slot -> slot.method_7677().method_7909().equals(item)).filter(filter).max(comparator).orElse(null);
   }

   public static Stream<class_1735> slots() {
      return PlayerInteractionHelper.nullCheck() ? Stream.empty() : mc.field_1724.field_7512.field_7761.stream();
   }

   private static void clickSlot(int syncId, int slotId, int buttonId, class_1713 clickType) {
      if (!PlayerInteractionHelper.nullCheck() && mc.field_1761 != null) {
         mc.field_1761.method_2906(syncId, slotId, buttonId, clickType, mc.field_1724);
      }
   }
}
