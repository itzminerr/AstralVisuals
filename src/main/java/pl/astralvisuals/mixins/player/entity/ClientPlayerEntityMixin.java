package pl.astralvisuals.mixins.player.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_1313;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_742;
import net.minecraft.class_744;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.block.PushEvent;
import pl.astralvisuals.events.container.CloseScreenEvent;
import pl.astralvisuals.features.impl.movement.LockSlot;
import pl.astralvisuals.events.player.MotionEvent;
import pl.astralvisuals.events.player.MoveEvent;
import pl.astralvisuals.events.player.PlayerTravelEvent;
import pl.astralvisuals.events.player.PostMotionEvent;
import pl.astralvisuals.events.player.PostTickEvent;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_746.class)
public abstract class ClientPlayerEntityMixin extends class_742 {
   @Final
   @Shadow
   protected class_310 field_3937;
   @Shadow
   public class_744 field_3913;

   @Shadow
   public abstract float method_5695(float var1);

   @Shadow
   public abstract float method_5705(float var1);

   @Shadow
   protected abstract void method_3148(float var1, float var2);

   public ClientPlayerEntityMixin(class_638 world, GameProfile profile) {
      super(world, profile);
   }

   @Inject(method = "tick", at = @At("HEAD"))
   public void tick(CallbackInfo info) {
      if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
         EventManager.callEvent(new TickEvent());
      }
   }

   @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = Shift.AFTER))
   public void postTick(CallbackInfo callbackInfo) {
      if (this.field_3937.field_1724 != null && this.field_3937.field_1687 != null) {
         EventManager.callEvent(new PostTickEvent());
      }
   }

   @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Input;tick()V", shift = Shift.AFTER))
   private void onInputTick(CallbackInfo ci) {
      if (QuickImports.mc.field_1724 != null) {
         EventManager.callEvent(new PlayerTravelEvent(class_243.field_1353, false));
      }
   }

   @Inject(method = "closeHandledScreen", at = @At("HEAD"), cancellable = true)
   private void closeHandledScreenHook(CallbackInfo info) {
      CloseScreenEvent event = new CloseScreenEvent(this.field_3937.field_1755);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         info.cancel();
      }
   }

   @Inject(method = "sendMovementPackets", at = @At("HEAD"), cancellable = true)
   private void preMotion(CallbackInfo ci) {
      MotionEvent event = new MotionEvent(
         this.method_23317(), this.method_23318(), this.method_23321(), this.method_5705(1.0F), this.method_5695(1.0F), this.method_24828()
      );
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = "move",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"
      ),
      cancellable = true
   )
   public void onMoveHook(class_1313 movementType, class_243 movement, CallbackInfo ci) {
      MoveEvent event = new MoveEvent(movement);
      EventManager.callEvent(event);
      double d = this.method_23317();
      double e = this.method_23321();
      super.method_5784(movementType, event.getMovement());
      this.method_3148((float)(this.method_23317() - d), (float)(this.method_23321() - e));
      ci.cancel();
   }

   @Inject(method = "sendMovementPackets", at = @At("RETURN"))
   private void postMotion(CallbackInfo ci) {
      EventManager.callEvent(new PostMotionEvent());
   }

   @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         ci.cancel();
      }
   }

   // LockSlot: запрет выброса предмета (Q) из заблокированного слота. method_7290(Z)Z = dropSelectedItem.
   @Inject(method = "method_7290(Z)Z", at = @At("HEAD"), cancellable = true, remap = false)
   private void astral$lockSlotDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      LockSlot lock = LockSlot.getInstance();
      if (lock != null && lock.isState() && lock.isSlotLocked(this.method_31548().field_7545)) {
         cir.setReturnValue(false);
      }
   }
}
