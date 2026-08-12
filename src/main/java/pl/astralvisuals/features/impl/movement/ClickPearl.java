package pl.astralvisuals.features.impl.movement;

import net.minecraft.class_1268;
import net.minecraft.class_1661;
import net.minecraft.class_1802;
import net.minecraft.class_2868;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BindSetting;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public class ClickPearl extends Module {
   private final BindSetting throwKey = new BindSetting("Клавиша", "Клавиша для броска эндер-жемчуга");
   private final BooleanSetting swapBack = new BooleanSetting("Возврат слота", "Вернуть прошлый слот после броска").setValue(true);
   private final BooleanSetting offhandSetting = new BooleanSetting("Из второй руки", "Бросать из второй руки, если там есть жемчуг").setValue(true);

   private boolean wasKeyDown;
   private int phase;
   private int previousSlot = -1;
   private long lastThrow;

   public static ClickPearl getInstance() {
      return Instance.get(ClickPearl.class);
   }

   public ClickPearl() {
      super("ClickPearl", "Click Pearl", ModuleCategory.PLAYER);
      this.setup(this.throwKey, this.swapBack, this.offhandSetting);
   }

   @Override
   public void deactivate() {
      if (this.previousSlot != -1 && !PlayerInteractionHelper.nullCheck()) {
         this.selectSlot(this.previousSlot);
      }

      this.reset();
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (PlayerInteractionHelper.nullCheck() || mc.field_1761 == null) {
         this.reset();
         return;
      }

      boolean down = this.isThrowKeyDown();
      boolean pressed = down && !this.wasKeyDown;
      this.wasKeyDown = down;

      switch (this.phase) {
         case 1:
            this.throwPearl(class_1268.field_5808);
            this.phase = this.swapBack.isValue() ? 2 : 0;
            if (this.phase == 0) {
               this.previousSlot = -1;
               this.lastThrow = System.currentTimeMillis();
            }
            break;
         case 2:
            if (this.previousSlot != -1) {
               this.selectSlot(this.previousSlot);
               this.previousSlot = -1;
            }

            this.phase = 0;
            this.lastThrow = System.currentTimeMillis();
            break;
         default:
            if (pressed) {
               this.startThrow();
            }
      }
   }

   private void startThrow() {
      class_1661 inv = mc.field_1724.method_31548();
      if (this.offhandSetting.isValue() && mc.field_1724.method_6079().method_31574(class_1802.field_8634)) {
         this.throwPearl(class_1268.field_5810);
         this.lastThrow = System.currentTimeMillis();
         return;
      }

      int pearlSlot = this.findPearlSlot(inv);
      if (pearlSlot == -1) {
         return;
      }

      int current = inv.field_7545;
      if (pearlSlot == current) {
         this.throwPearl(class_1268.field_5808);
         this.lastThrow = System.currentTimeMillis();
      } else {
         this.previousSlot = current;
         this.selectSlot(pearlSlot);
         this.phase = 1;
      }
   }

   private void throwPearl(class_1268 hand) {
      PlayerInteractionHelper.interactItem(hand);
   }

   private int findPearlSlot(class_1661 inv) {
      for (int i = 0; i < 9; i++) {
         if (inv.method_5438(i).method_31574(class_1802.field_8634)) {
            return i;
         }
      }

      return -1;
   }

   private void selectSlot(int slot) {
      mc.field_1724.method_31548().field_7545 = slot;
      mc.field_1724.field_3944.method_52787(new class_2868(slot));
   }

   private boolean isThrowKeyDown() {
      int key = this.throwKey.getKey();
      return key != -1 && mc.field_1755 == null && PlayerInteractionHelper.isKey(PlayerInteractionHelper.getKeyType(key), key);
   }

   private void reset() {
      this.phase = 0;
      this.previousSlot = -1;
      this.wasKeyDown = false;
   }
}
