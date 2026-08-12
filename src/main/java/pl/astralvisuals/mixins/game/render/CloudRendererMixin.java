package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_243;
import net.minecraft.class_4063;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.NoRender;

// Скрытие облаков в NoRender. class_9955 = CloudRenderer.
@Mixin(net.minecraft.class_9955.class)
public class CloudRendererMixin {
   // method_62168 = renderClouds(int, CloudRenderMode, float, Matrix4f, Matrix4f, Vec3d, float) — точка входа.
   @Inject(
      method = "method_62168(ILnet/minecraft/class_4063;FLorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/class_243;F)V",
      at = @At("HEAD"), cancellable = true, remap = false
   )
   private void astral$noClouds(
      int color, class_4063 cloudRenderMode, float cloudHeight, Matrix4f modelView, Matrix4f projection,
      class_243 cameraPos, float ticks, CallbackInfo ci
   ) {
      NoRender noRender = NoRender.getInstance();
      if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected(NoRender.CLOUDS)) {
         ci.cancel();
      }
   }
}
