package pl.astralvisuals.mixins.player.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.block.PushEvent;
import pl.astralvisuals.events.player.KeepSprintEvent;
import pl.astralvisuals.events.player.PlayerTravelEvent;
import pl.astralvisuals.events.player.SwimmingEvent;
import pl.astralvisuals.features.impl.movement.HitSound;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_1657.class)
public abstract class PlayerEntityMixin implements QuickImports {
   @Redirect(
      method = "method_7324(Lnet/minecraft/class_1297;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1937;method_54762(Lnet/minecraft/class_1657;DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;)V"
      ),
      require = 0,
      remap = false
   )
   private void astral$suppressAttackSound(class_1937 world, class_1657 except, double x, double y, double z, class_3414 sound, class_3419 category) {
      HitSound module = HitSound.getInstance();
      if ((Object)this == mc.field_1724 && module != null && module.shouldSuppressDefaults() && HitSound.isAttackSound(sound)) {
         return;
      }
      world.method_54762(except, x, y, z, sound, category);
   }

   @Redirect(
      method = "method_7324(Lnet/minecraft/class_1297;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1937;method_43128(Lnet/minecraft/class_1657;DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V"
      ),
      require = 0,
      remap = false
   )
   private void astral$suppressAttackSoundWithVolume(class_1937 world, class_1657 except, double x, double y, double z, class_3414 sound, class_3419 category, float volume, float pitch) {
      HitSound module = HitSound.getInstance();
      if ((Object)this == mc.field_1724 && module != null && module.shouldSuppressDefaults() && HitSound.isAttackSound(sound)) {
         return;
      }
      world.method_43128(except, x, y, z, sound, category, volume, pitch);
   }

   @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
   public void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
      PushEvent event = new PushEvent(PushEvent.Type.WATER);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = Shift.AFTER))
   public void attackHook(CallbackInfo callbackInfo) {
      EventManager.callEvent(new KeepSprintEvent());
   }

   @ModifyExpressionValue(
      method = "travel",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;")
   )
   public class_243 travelHook(class_243 vec3d) {
      SwimmingEvent event = new SwimmingEvent(vec3d);
      EventManager.callEvent(event);
      return event.getVector();
   }

   @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
   private void onTravelPre(class_243 movementInput, CallbackInfo ci) {
      if (mc.field_1724 != null) {
         PlayerTravelEvent event = new PlayerTravelEvent(movementInput, true);
         EventManager.callEvent(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "travel", at = @At("RETURN"))
   private void onTravelPost(class_243 movementInput, CallbackInfo ci) {
      if (mc.field_1724 != null) {
         PlayerTravelEvent event = new PlayerTravelEvent(movementInput, false);
         EventManager.callEvent(event);
      }
   }

}
