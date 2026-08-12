package pl.astralvisuals.mixins.game.world;

import net.minecraft.class_3695;
import net.minecraft.class_4184;
import net.minecraft.class_4604;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9909;
import net.minecraft.class_9958;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.astralvisuals.features.impl.render.BlockOverlay;

@Mixin(class_761.class)
public abstract class WorldRendererMixin {
   @Shadow
   protected abstract void method_62202(
      class_9909 var1,
      class_4604 var2,
      class_4184 var3,
      Matrix4f var4,
      Matrix4f var5,
      class_9958 var6,
      boolean var7,
      boolean var8,
      class_9779 var9,
      class_3695 var10
   );

   @Redirect(
      method = "render",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/Fog;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V"
      )
   )
   private void onRender(
      class_761 instance,
      class_9909 frameGraphBuilder,
      class_4604 frustum,
      class_4184 camera,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      class_9958 fog,
      boolean renderBlockOutline,
      boolean hasEntitiesToRender,
      class_9779 renderTickCounter,
      class_3695 profiler
   ) {
      this.method_62202(
         frameGraphBuilder,
         frustum,
         camera,
         positionMatrix,
         projectionMatrix,
         fog,
         !BlockOverlay.getInstance().isState(),
         hasEntitiesToRender,
         renderTickCounter,
         profiler
      );
   }
}
