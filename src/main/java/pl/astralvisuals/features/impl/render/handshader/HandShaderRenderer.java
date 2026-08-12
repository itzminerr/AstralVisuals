package pl.astralvisuals.features.impl.render.handshader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_276;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5944;
import net.minecraft.class_6367;
import net.minecraft.class_293.class_5596;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import pl.astralvisuals.features.impl.render.HandShader;

/**
 * Строит маску рук по разнице depth-буферов до и после first-person рендера,
 * затем накладывает цвет, обводку и свечение отдельным post-process проходом.
 */
public final class HandShaderRenderer {
   public static final HandShaderRenderer INSTANCE = new HandShaderRenderer();

   private static final class_10156 MASK_SHADER = new class_10156(
      class_2960.method_60654("core/hands/hands_mask_diff"), class_290.field_1575, class_10149.field_53930
   );
   private static final class_10156 COMPOSITE_SHADER = new class_10156(
      class_2960.method_60654("core/hands/hands_composite"), class_290.field_1575, class_10149.field_53930
   );

   private final class_310 client = class_310.method_1551();
   private class_276 beforeBuffer;
   private class_276 afterBuffer;
   private class_276 maskBuffer;
   private int width = -1;
   private int height = -1;
   private int configuredBeforeDepth = -1;
   private int configuredAfterDepth = -1;
   private boolean hasBeforeCapture;
   private boolean pendingComposite;
   private long startMillis = -1L;

   private HandShaderRenderer() {
   }

   public void captureBeforeHands(HandShader module) {
      if (module == null || !module.isEffectEnabled() || this.client.method_1522() == null) {
         this.invalidateState();
         return;
      }

      this.ensureBuffers();
      if (this.beforeBuffer == null) {
         return;
      }

      this.copyMainFramebuffer(this.beforeBuffer);
      this.hasBeforeCapture = true;
   }

   public void captureAfterHands(HandShader module) {
      if (module == null || !module.isEffectEnabled()) {
         this.invalidateState();
         return;
      }

      this.ensureBuffers();
      if (!this.hasBeforeCapture || this.afterBuffer == null || this.maskBuffer == null) {
         return;
      }

      this.copyMainFramebuffer(this.afterBuffer);
      this.pendingComposite = true;
   }

   public void renderOverlayIfPending(HandShader module) {
      if (!this.pendingComposite) {
         return;
      }

      if (module == null || !module.isEffectEnabled()) {
         this.invalidateState();
         return;
      }

      this.ensureBuffers();
      if (this.beforeBuffer == null || this.afterBuffer == null || this.maskBuffer == null) {
         this.invalidateState();
         return;
      }

      try {
         this.renderMask();
         this.renderComposite(module);
      } finally {
         this.restoreRenderState();
         this.invalidateState();
      }
   }

   private void renderMask() {
      this.maskBuffer.method_1236(0.0F, 0.0F, 0.0F, 0.0F);
      this.maskBuffer.method_1230();
      this.maskBuffer.method_1235(false);
      RenderSystem.disableDepthTest();
      RenderSystem.disableBlend();

      class_5944 shader = RenderSystem.setShader(MASK_SHADER);
      if (shader == null) {
         return;
      }

      int beforeDepth = this.beforeBuffer.method_30278();
      int afterDepth = this.afterBuffer.method_30278();
      if (beforeDepth != 0 && beforeDepth != this.configuredBeforeDepth) {
         this.configureDepthTexture(beforeDepth);
         this.configuredBeforeDepth = beforeDepth;
      }
      if (afterDepth != 0 && afterDepth != this.configuredAfterDepth) {
         this.configureDepthTexture(afterDepth);
         this.configuredAfterDepth = afterDepth;
      }

      RenderSystem.setShaderTexture(0, beforeDepth);
      RenderSystem.setShaderTexture(1, afterDepth);
      this.drawFullscreenQuad();
   }

   private void renderComposite(HandShader module) {
      this.client.method_1522().method_1235(true);
      RenderSystem.disableDepthTest();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();

      class_5944 shader = RenderSystem.setShader(COMPOSITE_SHADER);
      if (shader == null) {
         return;
      }

      RenderSystem.setShaderTexture(0, this.maskBuffer.method_30277());
      int primary = module.getPrimaryColor();
      int secondary = module.getSecondaryColor();
      shader.method_34582("Color1").method_1249(red(primary), green(primary), blue(primary));
      shader.method_34582("Color2").method_1249(red(secondary), green(secondary), blue(secondary));
      shader.method_34582("TexelSize").method_1255(1.0F / Math.max(1, this.width), 1.0F / Math.max(1, this.height));
      shader.method_34582("Time").method_1251(this.elapsedSeconds());
      shader.method_34582("WaveSpeed").method_1251(module.getWaveSpeed());
      shader.method_34582("WaveScale").method_1251(module.getWaveScale());
      shader.method_34582("Outline").method_1251(module.getOutline());
      shader.method_34582("Glow").method_1251(module.getGlow());
      shader.method_34582("Fill").method_1251(module.getFill());
      shader.method_34582("Alpha").method_1251(module.getAlpha());
      shader.method_34582("Mode").method_1251(module.isPrettyMode() ? 1.0F : 0.0F);
      this.drawFullscreenQuad();
   }

   private float elapsedSeconds() {
      if (this.startMillis < 0L) {
         this.startMillis = System.currentTimeMillis();
      }
      return (System.currentTimeMillis() - this.startMillis) / 1000.0F;
   }

   private void ensureBuffers() {
      int newWidth = this.client.method_22683().method_4489();
      int newHeight = this.client.method_22683().method_4506();
      if (newWidth <= 0 || newHeight <= 0) {
         return;
      }
      if (newWidth == this.width && newHeight == this.height
         && this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
         return;
      }

      this.deleteBuffers();
      this.beforeBuffer = new class_6367(newWidth, newHeight, true);
      this.afterBuffer = new class_6367(newWidth, newHeight, true);
      this.maskBuffer = new class_6367(newWidth, newHeight, false);
      this.width = newWidth;
      this.height = newHeight;
      this.configuredBeforeDepth = -1;
      this.configuredAfterDepth = -1;
      this.hasBeforeCapture = false;
      this.pendingComposite = false;
   }

   private void deleteBuffers() {
      if (this.beforeBuffer != null) {
         this.beforeBuffer.method_1238();
      }
      if (this.afterBuffer != null) {
         this.afterBuffer.method_1238();
      }
      if (this.maskBuffer != null) {
         this.maskBuffer.method_1238();
      }
      this.beforeBuffer = null;
      this.afterBuffer = null;
      this.maskBuffer = null;
   }

   private void copyMainFramebuffer(class_276 target) {
      class_276 main = this.client.method_1522();
      int previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      int previousDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, main.field_1476);
      GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.field_1476);
      GL30.glBlitFramebuffer(
         0, 0, this.width, this.height,
         0, 0, this.width, this.height,
         GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT,
         GL11.GL_NEAREST
      );
      GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
      GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
      main.method_1235(true);
   }

   private void configureDepthTexture(int texture) {
      RenderSystem.bindTexture(texture);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
      RenderSystem.bindTexture(0);
   }

   private void drawFullscreenQuad() {
      float scaledWidth = Math.max(1, this.client.method_22683().method_4486());
      float scaledHeight = Math.max(1, this.client.method_22683().method_4502());
      class_287 builder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      builder.method_22912(0.0F, 0.0F, 0.0F).method_22913(0.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(0.0F, scaledHeight, 0.0F).method_22913(0.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(scaledWidth, scaledHeight, 0.0F).method_22913(1.0F, 0.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      builder.method_22912(scaledWidth, 0.0F, 0.0F).method_22913(1.0F, 1.0F).method_22915(1.0F, 1.0F, 1.0F, 1.0F);
      class_286.method_43433(builder.method_60800());
   }

   private void restoreRenderState() {
      if (this.client.method_1522() != null) {
         this.client.method_1522().method_1235(true);
      }
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.setShaderTexture(1, 0);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void invalidateState() {
      this.hasBeforeCapture = false;
      this.pendingComposite = false;
   }

   private static float red(int color) {
      return (color >> 16 & 255) / 255.0F;
   }

   private static float green(int color) {
      return (color >> 8 & 255) / 255.0F;
   }

   private static float blue(int color) {
      return (color & 255) / 255.0F;
   }
}
