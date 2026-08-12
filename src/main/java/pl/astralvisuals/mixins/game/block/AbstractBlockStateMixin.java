package pl.astralvisuals.mixins.game.block;

import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_4970.class_4971;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.player.PlayerCollisionEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

@Mixin(class_4971.class)
public abstract class AbstractBlockStateMixin implements QuickImports {
   @Shadow
   public abstract class_2248 method_26204();

   @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
   public void onEntityCollision(class_1937 world, class_2338 pos, class_1297 entity, CallbackInfo ci) {
      if (entity == mc.field_1724) {
         PlayerCollisionEvent event = new PlayerCollisionEvent(this.method_26204());
         EventManager.callEvent(event);
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
