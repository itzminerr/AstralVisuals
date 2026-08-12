package pl.astralvisuals.features.impl.movement;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public class ItemScroller extends Module {
   // слоты, уже обработанные в текущем драге (по индексу слота в хендлере)
   private final Set<Integer> processed = new HashSet<>();

   public static ItemScroller getInstance() {
      return Instance.get(ItemScroller.class);
   }

   public ItemScroller() {
      super("ItemScroller", "Item Scroller", ModuleCategory.PLAYER);
   }

   @Override
   public void deactivate() {
      this.processed.clear();
   }

   // ЛКМ нажата с Shift -> начинается новый драг. Первый слот ванила прожмёт сама (shift-click),
   // поэтому помечаем его обработанным, чтобы драг не перетащил его повторно.
   public void onDragStart(class_1735 initialSlot) {
      this.processed.clear();
      if (initialSlot != null) {
         this.processed.add(initialSlot.field_7874);
      }
   }

   // во время драга мышь прошла над слотом -> быстрый перенос (как shift-click), один раз на слот
   public void onSlotDragged(class_1735 slot) {
      if (PlayerInteractionHelper.nullCheck() || mc.field_1761 == null || mc.field_1724 == null) {
         return;
      }

      if (slot == null || !slot.method_7681()) {
         return;
      }

      class_1703 handler = mc.field_1724.field_7512;
      if (handler == null) {
         return;
      }

      if (this.processed.add(slot.field_7874)) {
         mc.field_1761.method_2906(handler.field_7763, slot.field_7874, 0, class_1713.field_7794, mc.field_1724);
      }
   }
}
