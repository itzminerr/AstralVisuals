package pl.astralvisuals.mixins.player.entity;

import net.minecraft.class_1282;
import net.minecraft.class_1291;
import net.minecraft.class_1292;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2394;
import net.minecraft.class_310;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.block.PushEvent;
import pl.astralvisuals.events.item.SwingDurationEvent;
import pl.astralvisuals.events.player.EntityDeathEvent;
import pl.astralvisuals.events.player.JumpEvent;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.managers.event.EventManager;

@Mixin(class_1309.class)
public abstract class LivingEntityMixin {
   @Unique
   private final class_310 client = class_310.method_1551();

   @Shadow
   public abstract boolean method_6059(class_6880<class_1291> var1);

   @Shadow
   @Nullable
   public abstract class_1293 method_6112(class_6880<class_1291> var1);

   @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
   public void isPushable(CallbackInfoReturnable<Boolean> infoReturnable) {
      PushEvent event = new PushEvent(PushEvent.Type.COLLISION);
      EventManager.callEvent(event);
      if (event.isCancelled()) {
         infoReturnable.setReturnValue(false);
      }
   }

   @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
   private void jump(CallbackInfo info) {
      if ((Object)this instanceof class_746 player) {
         JumpEvent event = new JumpEvent(player);
         EventManager.callEvent(event);
         if (event.isCancelled()) {
            info.cancel();
         }
      }
   }

   @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
   private void swingProgressHook(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == this.client.field_1724) {
         SwingDurationEvent event = new SwingDurationEvent();
         EventManager.callEvent(event);
         if (event.isCancelled()) {
            float animation = event.getAnimation();
            if (class_1292.method_5576(this.client.field_1724)) {
               animation *= 6 - (1 + class_1292.method_5575(this.client.field_1724));
            } else {
               animation *= this.method_6059(class_1294.field_5901) ? 6 + (1 + this.method_6112(class_1294.field_5901).method_5578()) * 2 : 6.0F;
            }

            cir.setReturnValue((int)animation);
         }
      }
   }

   @Inject(method = "onDeath", at = @At("HEAD"))
   private void onDeath(class_1282 source, CallbackInfo ci) {
      class_1309 entity = (class_1309)(Object)this;
      EventManager.callEvent(new EntityDeathEvent(entity, source));
   }

   @Inject(method = "handleStatus", at = @At("HEAD"))
   private void handleStatus(byte status, CallbackInfo ci) {
      if (status == 3) {
         class_1309 entity = (class_1309)(Object)this;
         EventManager.callEvent(new EntityDeathEvent(entity, null));
      }
   }

   // method_6050 = tickStatusEffects: внутри спавнит партиклы эффектов через World.addParticle.
   // Гасим их при включённом NoRender -> "Партиклы эффектов" (intermediary + remap=false для нового члена).
   @Redirect(
      method = "method_6050",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8406(Lnet/minecraft/class_2394;DDDDDD)V", remap = false),
      remap = false
   )
   private void astral$noEffectParticles(
      class_1937 world, class_2394 particle, double x, double y, double z, double velocityX, double velocityY, double velocityZ
   ) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Партиклы эффектов")) {
         return;
      }

      world.method_8406(particle, x, y, z, velocityX, velocityY, velocityZ);
   }
}
