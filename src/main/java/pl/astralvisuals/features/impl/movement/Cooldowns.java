package pl.astralvisuals.features.impl.movement;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2656;
import net.minecraft.class_2724;
import net.minecraft.class_332;
import net.minecraft.class_465;
import net.minecraft.class_7923;
import pl.astralvisuals.events.container.HandledScreenEvent;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.events.render.DrawEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.BooleanSetting;
import pl.astralvisuals.features.module.setting.implement.ColorSetting;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.display.color.ColorAssist;

/** Показывает серверный кулдаун прямо поверх предметов в хотбаре и открытом инвентаре. */
public class Cooldowns extends Module {
   private final BooleanSetting hotbar = new BooleanSetting("Хотбар", "Показывать отсчёт на слотах хотбара").setValue(true);
   private final BooleanSetting inventory = new BooleanSetting("Инвентарь", "Показывать отсчёт в открытом инвентаре").setValue(true);
   private final BooleanSetting background = new BooleanSetting("Подложка", "Затемнять фон под таймером").setValue(true);
   private final ColorSetting textColor = new ColorSetting("Цвет текста", "Цвет таймера кулдауна").setColor(-1);
   private final Map<class_1792, Long> cooldownEnds = new HashMap<>();

   public Cooldowns() {
      super("Cooldowns", "Cooldowns", ModuleCategory.PLAYER);
      this.setup(this.hotbar, this.inventory, this.background, this.textColor);
   }

   public static Cooldowns getInstance() {
      return Instance.get(Cooldowns.class);
   }

   @Override
   public void deactivate() {
      this.cooldownEnds.clear();
   }

   @EventHandler
   public void onTick(TickEvent event) {
      long now = System.currentTimeMillis();
      Iterator<Entry<class_1792, Long>> iterator = this.cooldownEnds.entrySet().iterator();
      while (iterator.hasNext()) {
         if (iterator.next().getValue() <= now) {
            iterator.remove();
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof class_2656 packet) {
         class_1792 item = (class_1792)class_7923.field_41178.method_63535(packet.comp_3082());
         if (item == null) {
            return;
         }

         if (packet.comp_2199() <= 0) {
            this.cooldownEnds.remove(item);
         } else {
            this.cooldownEnds.put(item, System.currentTimeMillis() + packet.comp_2199() * 50L);
         }
      } else if (event.getPacket() instanceof class_2724) {
         this.cooldownEnds.clear();
      }
   }

   @EventHandler
   public void onDraw(DrawEvent event) {
      if (!this.hotbar.isValue() || mc.field_1724 == null || mc.field_1755 instanceof class_465 || this.cooldownEnds.isEmpty()) {
         return;
      }

      int hotbarX = mc.method_22683().method_4486() / 2 - 91;
      int hotbarY = mc.method_22683().method_4502() - 22;
      for (int slot = 0; slot < 9; slot++) {
         this.drawForStack(event.getDrawContext(), mc.field_1724.method_31548().method_5438(slot), hotbarX + slot * 20 + 1, hotbarY + 1);
      }
   }

   @EventHandler
   public void onHandledScreen(HandledScreenEvent event) {
      if (!this.inventory.isValue() || mc.field_1724 == null || this.cooldownEnds.isEmpty()) {
         return;
      }

      class_1703 handler = mc.field_1724.field_7512;
      if (handler == null) {
         return;
      }

      int left = (mc.method_22683().method_4486() - event.getBackgroundWidth()) / 2;
      int top = (mc.method_22683().method_4502() - event.getBackgroundHeight()) / 2;
      for (class_1735 slot : handler.field_7761) {
         if (slot != null) {
            this.drawForStack(event.getDrawContext(), slot.method_7677(), left + slot.field_7873, top + slot.field_7872);
         }
      }
   }

   private void drawForStack(class_332 context, class_1799 stack, int slotX, int slotY) {
      if (stack == null || stack.method_7960()) {
         return;
      }

      Long end = this.cooldownEnds.get(stack.method_7909());
      long remaining = end == null ? 0L : end - System.currentTimeMillis();
      if (remaining <= 0L) {
         return;
      }

      String text = Math.max(1, (int)Math.ceil(remaining / 1000.0)) + "с";
      int width = mc.field_1772.method_1727(text);
      float scale = Math.min(1.0F, 15.0F / Math.max(1, width));
      float drawX = slotX + 9.0F - width * scale / 2.0F;
      float drawY = slotY + 7.0F;
      if (this.background.isValue()) {
         context.method_25294((int)drawX - 1, (int)drawY - 1, (int)(drawX + width * scale) + 1, (int)(drawY + 9 * scale), ColorAssist.HALF_BLACK);
      }

      context.method_51448().method_22903();
      context.method_51448().method_46416(drawX, drawY, 500.0F);
      context.method_51448().method_22905(scale, scale, 1.0F);
      context.method_25303(mc.field_1772, text, 0, 0, this.textColor.getColor());
      context.method_51448().method_22909();
   }
}
