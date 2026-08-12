package pl.astralvisuals.mixins.player.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_1309;
import net.minecraft.class_1671;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import pl.astralvisuals.events.player.FireworkEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_1671.class)
public class FireworkRocketEntityMixin {
   @Shadow
   @Nullable
   private class_1309 field_7616;

   @WrapOperation(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;", ordinal = 0)
   )
   public class_243 getVelocityHook(class_1309 instance, Operation<class_243> original) {
      if (this.field_7616 != null) {
         FireworkEvent event = new FireworkEvent((class_243)original.call(new Object[]{instance}));
         EventManager.callEvent(event);
         return event.getVector();
      } else {
         return (class_243)original.call(new Object[]{instance});
      }
   }
}
