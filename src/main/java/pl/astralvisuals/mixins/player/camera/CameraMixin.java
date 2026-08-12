package pl.astralvisuals.mixins.player.camera;

import net.minecraft.class_1297;
import net.minecraft.class_1922;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_746;
import net.minecraft.class_2338.class_2339;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.events.render.CameraEvent;
import pl.astralvisuals.events.render.CameraPositionEvent;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.player.rotation.Turns;

@Mixin(class_4184.class)
public abstract class CameraMixin {
   @Shadow
   private class_243 field_18712;
   @Shadow
   @Final
   private class_2339 field_18713;
   @Shadow
   private float field_18718;
   @Shadow
   private float field_18717;

   @Shadow
   public abstract void method_19325(float var1, float var2);

   @Shadow
   protected abstract void method_19324(float var1, float var2, float var3);

   @Shadow
   protected abstract float method_19318(float var1);

   @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = Shift.AFTER), cancellable = true)
   private void updateHook(class_1922 area, class_1297 focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
      CameraEvent event = new CameraEvent(false, 4.0F, new Turns(this.field_18718, this.field_18717));
      EventManager.callEvent(event);
      Turns angle = event.getAngle();
      if (event.isCancelled() && focusedEntity instanceof class_746 player && !player.method_6113() && thirdPerson) {
         float pitch = inverseView ? -angle.getPitch() : angle.getPitch();
         float yaw = angle.getYaw() - (inverseView ? 180 : 0);
         float distance = event.getDistance();
         this.method_19325(yaw, pitch);
         this.method_19324(event.isCameraClip() ? -distance : -this.method_19318(distance), 0.0F, 0.0F);
         ci.cancel();
      }
   }

   @Inject(method = "setPos(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
   private void posHook(class_243 pos, CallbackInfo ci) {
      CameraPositionEvent event = new CameraPositionEvent(pos);
      EventManager.callEvent(event);
      this.field_18712 = event.getPos();
      this.field_18713.method_10102(this.field_18712.field_1352, this.field_18712.field_1351, this.field_18712.field_1350);
      ci.cancel();
   }
}
