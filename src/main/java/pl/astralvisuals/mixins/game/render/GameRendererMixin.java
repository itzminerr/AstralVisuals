package pl.astralvisuals.mixins.game.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4608;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.astralvisuals.events.render.AspectRatioEvent;
import pl.astralvisuals.events.render.FovEvent;
import pl.astralvisuals.events.render.WorldRenderEvent;
import pl.astralvisuals.features.impl.render.HitColor;
import pl.astralvisuals.features.impl.render.NoRender;
import pl.astralvisuals.utils.client.interfaces.IOverlayTexture;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.geometry.Render3D;

@Mixin(class_757.class)
public abstract class GameRendererMixin {
   @Final
   @Shadow
   private class_310 field_4015;
   @Shadow
   private float field_4005;
   @Shadow
   private float field_3988;
   @Shadow
   private float field_4004;
   @Final
   @Shadow
   private class_4608 field_20949;

   @Shadow
   public abstract float method_32796();

   @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
   public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
      AspectRatioEvent aspectRatioEvent = new AspectRatioEvent();
      EventManager.callEvent(aspectRatioEvent);
      if (aspectRatioEvent.isCancelled()) {
         Matrix4f matrix4f = new Matrix4f();
         if (this.field_4005 != 1.0F) {
            matrix4f.translate(this.field_3988, -this.field_4004, 0.0F);
            matrix4f.scale(this.field_4005, this.field_4005, 1.0F);
         }

         matrix4f.perspective(fovDegrees * (float) (Math.PI / 180.0), aspectRatioEvent.getRatio(), 0.05F, this.method_32796());
         cir.setReturnValue(matrix4f);
      }
   }

   @ModifyExpressionValue(method = "getFov", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;intValue()I", remap = false))
   private int hookGetFov(int original) {
      FovEvent event = new FovEvent();
      EventManager.callEvent(event);
      return event.isCancelled() ? event.getFov() : original;
   }

   @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = 180, ordinal = 0))
   public void hookWorldRender(class_9779 tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
      class_4587 matrixStack = new class_4587();
      matrixStack.method_34425(matrix4f);
      matrixStack.method_61958(this.field_4015.method_1561().field_4686.method_19326().method_22882());
      Render3D.setLastProjMat(RenderSystem.getProjectionMatrix());
      Render3D.setLastWorldSpaceMatrix(matrixStack.method_23760());
      WorldRenderEvent event = new WorldRenderEvent(matrixStack, tickCounter.method_60637(false));
      EventManager.callEvent(event);
      Render3D.onWorldRender(event);

      // HitColor: покадрово перекрашиваем вспышку урона. Перекраска (с upload текстуры) реально
      // происходит только при смене цвета — внутри astral$applyHitColor стоит сравнение с прошлым.
      HitColor hitColor = HitColor.getInstance();
      int hurtColor = hitColor != null && hitColor.isState() ? hitColor.getColor() : 0xB2FF0000;
      ((IOverlayTexture)(Object)this.field_20949).astral$applyHitColor(hurtColor);
   }

   @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
   private void onTiltViewWhenHurt(class_4587 matrices, float tickDelta, CallbackInfo ci) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Урон")) {
         ci.cancel();
      }
   }
}
