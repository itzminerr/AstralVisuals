package pl.astralvisuals.mixins.game.render;

import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_757;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.astralvisuals.features.impl.render.MotionBlur;

// class_761 = WorldRenderer. method_22710 = render(ObjectAllocator, RenderTickCounter, boolean, Camera, GameRenderer, Matrix4f, Matrix4f).
// Захватываем матрицы текущего и прошлого кадра для скоростного размытия. Имена intermediary + remap=false.
@Mixin(class_761.class)
public class MotionBlurMixin {
   @Unique
   private Matrix4f astral$prevModelView = new Matrix4f();
   @Unique
   private Matrix4f astral$prevProjection = new Matrix4f();
   @Unique
   private Vector3f astral$prevCameraPos = new Vector3f();

   @Inject(method = "method_22710", at = @At("HEAD"), remap = false)
   private void astral$motionBlurHead(
      class_9922 allocator,
      class_9779 tickCounter,
      boolean blockOutline,
      class_4184 camera,
      class_757 gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      MotionBlur module = MotionBlur.getInstance();
      if (module == null || !module.isState()) {
         return;
      }

      module.getShader()
         .setFrameMotionBlur(positionMatrix, this.astral$prevModelView, projectionMatrix, this.astral$prevProjection, this.cameraVec(camera), this.astral$prevCameraPos);
   }

   @Inject(method = "method_22710", at = @At("RETURN"), remap = false)
   private void astral$motionBlurReturn(
      class_9922 allocator,
      class_9779 tickCounter,
      boolean blockOutline,
      class_4184 camera,
      class_757 gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      MotionBlur module = MotionBlur.getInstance();
      if (module == null || !module.isState()) {
         return;
      }

      module.getShader().onFrame();
      this.astral$prevModelView = new Matrix4f(positionMatrix);
      this.astral$prevProjection = new Matrix4f(projectionMatrix);
      this.astral$prevCameraPos = this.cameraVec(camera);
   }

   @Unique
   private Vector3f cameraVec(class_4184 camera) {
      class_243 pos = camera.method_19326();
      return new Vector3f((float)(pos.field_1352 % 30000.0), (float)(pos.field_1351 % 30000.0), (float)(pos.field_1350 % 30000.0));
   }
}
