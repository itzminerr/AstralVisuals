package pl.astralvisuals.features.impl.movement;

import net.minecraft.class_1294;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.features.module.setting.implement.MultiSelectSetting;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public class AutoSprint extends Module {
   public static int tickStop;
   private final MultiSelectSetting settings = new MultiSelectSetting("Игнорировать", "Продолжать бег при эффектах").value("Замедление", "Слепота");

   public AutoSprint() {
      super("AutoSprint", "Auto Sprint", ModuleCategory.PLAYER);
      this.setup(this.settings);
   }

   @EventHandler
   public void onTick(TickEvent e) {
      if (!PlayerInteractionHelper.nullCheck()) {
         boolean hasSlowness = mc.field_1724.method_6059(class_1294.field_5909);
         boolean hasBlindness = mc.field_1724.method_6059(class_1294.field_5919);
         boolean blockedBySlowness = hasSlowness && !this.isSlownessIgnored();
         boolean blockedByBlindness = hasBlindness && !this.isBlindnessIgnored();
         boolean horizontalCollision = mc.field_1724.field_5976 && !mc.field_1724.field_34927;
         boolean sneaking = mc.field_1724.method_5715() && !mc.field_1724.method_5681();
         if (tickStop > 0 || sneaking || blockedBySlowness || blockedByBlindness) {
            mc.field_1724.method_5728(false);
         } else if (!horizontalCollision && mc.field_1724.field_6250 > 0.0F && !mc.field_1690.field_1867.method_1434()) {
            mc.field_1724.method_5728(true);
         }

         tickStop--;
      }
   }

   private boolean isSlownessIgnored() {
      return this.settings.isSelected("Замедление") || this.settings.isSelected("Slowness");
   }

   private boolean isBlindnessIgnored() {
      return this.settings.isSelected("Слепота") || this.settings.isSelected("Blindness");
   }
}
