package pl.astralvisuals.features.impl.render.motionblur;

import net.minecraft.class_276;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import pl.astralvisuals.features.impl.render.MotionBlur;

// Порт ShaderMotionBlur из EvaWare: управляемый пост-эффект (Satin) скоростного размытия.
public class ShaderMotionBlur {
   private final MotionBlur config;
   private final ManagedShaderEffect motionBlurShader;

   private long lastNano;
   private float currentBlur;
   private float currentFPS;

   private final Matrix4f tempPrevModelView = new Matrix4f();
   private final Matrix4f tempPrevProjection = new Matrix4f();
   private final Matrix4f tempProjInverse = new Matrix4f();
   private final Matrix4f tempMvInverse = new Matrix4f();

   public ShaderMotionBlur(MotionBlur config) {
      this.config = config;
      this.motionBlurShader = ShaderEffectManager.getInstance()
         .manage(class_2960.method_60654("motion_blur"), shader -> shader.setUniformValue("BlendFactor", config.getStrength()));
   }

   // Вызывается из MotionBlurMixin в конце рендера мира (RETURN) — кадр уже отрисован.
   public void onFrame() {
      long now = System.nanoTime();
      float deltaTime = (now - this.lastNano) / 1.0E9F;
      float deltaTick = deltaTime * 20.0F;
      this.lastNano = now;
      this.currentFPS = deltaTime > 0.0F && deltaTime < 1.0F ? 1.0F / deltaTime : 0.0F;
      if (this.shouldRender()) {
         this.applyMotionBlur(deltaTick);
      }
   }

   private boolean shouldRender() {
      return this.config.isState() && this.config.getStrength() != 0.0F;
   }

   private void applyMotionBlur(float deltaTick) {
      class_310 client = class_310.method_1551();
      MonitorInfoProvider.updateDisplayInfo();
      int refresh = MonitorInfoProvider.getRefreshRate();

      float base = this.config.getStrength();
      float scaled = base;
      if (this.config.useRefreshRateScaling()) {
         float ratio = refresh > 0 ? this.currentFPS / refresh : 1.0F;
         if (ratio < 1.0F) {
            ratio = 1.0F;
         }

         scaled = base * ratio;
      }

      if (this.currentBlur != scaled) {
         this.motionBlurShader.setUniformValue("BlendFactor", scaled);
         this.currentBlur = scaled;
      }

      int samples = this.sampleAmount(this.currentFPS);
      int half = samples / 2;
      float inv = 1.0F / samples;
      class_276 fb = client.method_1522();
      this.motionBlurShader.setUniformValue("view_res", (float)fb.field_1480, (float)fb.field_1481);
      this.motionBlurShader.setUniformValue("view_pixel_size", 1.0F / fb.field_1480, 1.0F / fb.field_1481);
      this.motionBlurShader.setUniformValue("motionBlurSamples", samples);
      this.motionBlurShader.setUniformValue("halfSamples", half);
      this.motionBlurShader.setUniformValue("inverseSamples", inv);
      this.motionBlurShader.setUniformValue("blurAlgorithm", 0);
      this.motionBlurShader.render(deltaTick);
   }

   private int sampleAmount(float fps) {
      if (fps > 360.0F) {
         return 8;
      } else if (fps > 120.0F) {
         return 10;
      } else {
         return fps > 60.0F ? 12 : 20;
      }
   }

   public void setFrameMotionBlur(
      Matrix4f modelView, Matrix4f prevModelView, Matrix4f projection, Matrix4f prevProjection, Vector3f cameraPos, Vector3f prevCameraPos
   ) {
      if (!this.shouldRender()) {
         return;
      }

      this.motionBlurShader.setUniformValue("mvInverse", this.tempMvInverse.set(modelView).invert());
      this.motionBlurShader.setUniformValue("projInverse", this.tempProjInverse.set(projection).invert());
      this.motionBlurShader.setUniformValue("prevModelView", this.tempPrevModelView.set(prevModelView));
      this.motionBlurShader.setUniformValue("prevProjection", this.tempPrevProjection.set(prevProjection));
      this.motionBlurShader.setUniformValue("cameraPos", cameraPos.x, cameraPos.y, cameraPos.z);
      this.motionBlurShader.setUniformValue("prevCameraPos", prevCameraPos.x, prevCameraPos.y, prevCameraPos.z);
   }

   public void updateStrength(float strength) {
      this.motionBlurShader.setUniformValue("BlendFactor", strength);
      this.currentBlur = strength;
   }
}
