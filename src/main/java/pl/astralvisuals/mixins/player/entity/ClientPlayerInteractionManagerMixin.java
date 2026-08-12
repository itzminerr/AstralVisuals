package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1713;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.block.BlockBreakingEvent;
import pl.astralvisuals.events.block.BreakBlockEvent;
import pl.astralvisuals.events.item.ClickSlotEvent;
import pl.astralvisuals.events.player.AttackEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_636.class)
public class ClientPlayerInteractionManagerMixin {
   @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
   public void attackEntityHook(class_1657 player, class_1297 target, CallbackInfo info) {
      AttackEvent event = new AttackEvent(target);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(method = "clickSlot", at = @At("HEAD"), cancellable = true)
   public void clickSlotHook(int syncId, int slotId, int button, class_1713 actionType, class_1657 player, CallbackInfo info) {
      ClickSlotEvent event = new ClickSlotEvent(syncId, slotId, button, actionType);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
   private void injectBlockBreaking(class_2338 pos, class_2350 direction, CallbackInfoReturnable<Boolean> cir) {
      EventManager.callEvent(new BlockBreakingEvent(pos, direction));
   }

   @Inject(method = "breakBlock", at = @At("RETURN"))
   private void injectBreakBlock(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      EventManager.callEvent(new BreakBlockEvent(pos));
   }
}
