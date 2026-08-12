package pl.astralvisuals.mixins.player.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_243;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.player.BoundingBoxControlEvent;
import pl.astralvisuals.events.player.PlayerVelocityStrafeEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_1297.class)
public abstract class EntityMixin implements QuickImports {
   @Shadow
   private class_238 field_6005;

   @Redirect(
      method = "updateVelocity",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"
      )
   )
   public class_243 hookVelocity(class_243 movementInput, float speed, float yaw) {
      if ((Object)this == mc.field_1724) {
         PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, class_1297.method_18795(movementInput, speed, yaw));
         EventManager.callEvent(event);
         return event.getVelocity();
      } else {
         return class_1297.method_18795(movementInput, speed, yaw);
      }
   }

   @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
   public final void getBoundingBox(CallbackInfoReturnable<class_238> cir) {
      BoundingBoxControlEvent event = new BoundingBoxControlEvent(this.field_6005, (class_1297)(Object)this);
      EventManager.callEvent(event);
      cir.setReturnValue(event.getBox());
   }

   @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isControlledByPlayer()Z"))
   public boolean isControlledByPlayerHook(boolean original) {
      return (Object)this == mc.field_1724 ? false : original;
   }
}
