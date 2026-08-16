package pl.astralvisuals.utils.display.geometry;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10142;
import net.minecraft.class_1058;
import net.minecraft.class_1799;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_9801;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.color.ColorAssist;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.item.ItemRender;
import pl.astralvisuals.utils.display.scale.UiScale;
import pl.astralvisuals.utils.display.shape.ShapeProperties;

public final class Render2D implements QuickImports {
   private static final List<Render2D.Quad> QUAD = new ArrayList<>();

   public static void onRender(class_332 context) {
      class_4587 matrix = context.method_51448();
      Matrix4f matrix4f = matrix.method_23760().method_23761();
      if (!QUAD.isEmpty()) {
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShader(class_10142.field_53876);
         class_287 buffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1576);
         QUAD.forEach(quad -> drawEngine.quad(matrix4f, buffer, quad.x, quad.y, quad.width, quad.height, quad.color));
         class_286.method_43433(buffer.method_60800());
         RenderSystem.disableBlend();
         QUAD.clear();
      }
   }

   public static void defaultDrawStack(class_332 context, class_1799 stack, float x, float y, boolean rect, boolean drawItemInSlot, float scale) {
      class_4587 matrix = context.method_51448();
      if (rect) {
         blur.render(ShapeProperties.create(matrix, x, y, 16.0F * scale + 2.0F, 16.0F * scale + 2.0F).round(2.0F).color(ColorAssist.HALF_BLACK).build());
      }

      matrix.method_22903();
      matrix.method_46416(UiScale.x(x + 1.0F), UiScale.y(y + 1.0F), 0.0F);
      float renderScale = scale * UiScale.scale();
      matrix.method_22905(renderScale, renderScale, 1.0F);
      context.method_51427(stack, 0, 0);
      if (drawItemInSlot) {
         context.method_51431(mc.field_1772, stack, 0, 0);
      }

      matrix.method_22909();
   }

   public static void drawStack(class_4587 matrix, class_1799 stack, float x, float y, boolean rect, float scale) {
      float posX = x + 1.0F;
      float posY = y + 1.0F;
      float padding = 1.0F;
      matrix.method_22903();
      matrix.method_46416(posX, posY, 0.0F);
      if (rect) {
         blur.render(
            ShapeProperties.create(matrix, -padding, -padding, 16.0F * scale + padding * 2.0F, 16.0F * scale + padding * 2.0F)
               .round(1.5F)
               .color(ColorAssist.HALF_BLACK)
               .build()
         );
      }

      matrix.method_22905(scale, scale, 1.0F);
      ItemRender.drawItem(matrix, stack, 0.0F, 0.0F, true, true);
      matrix.method_22909();
   }

   public static void drawTexture(class_332 context, class_2960 id, float x, float y, int size) {
      class_4587 matrix = context.method_51448();
      if (id != null) {
         float renderX = UiScale.x(x);
         float renderY = UiScale.y(y);
         float renderSize = UiScale.size(size);
         matrix.method_22903();
         matrix.method_46416(renderX, renderY, 0.0F);
         matrix.method_22905(renderSize, renderSize, 1.0F);
         RenderSystem.enableBlend();
         drawTexture(matrix, id, 0, 0, 1.0F, 1.0F, size, size, size, size, size, size, -1);
         RenderSystem.disableBlend();
         matrix.method_22909();
      }
   }

   /** Draws a texture with its original RGB values and standard alpha blending. */
   public static void drawTextureOriginal(class_332 context, class_2960 id, float x, float y, float width, float height) {
      if (id == null) {
         return;
      }
      class_4587 matrix = context.method_51448();
      float renderX = UiScale.x(x);
      float renderY = UiScale.y(y);
      float renderWidth = UiScale.size(width);
      float renderHeight = UiScale.size(height);
      matrix.method_22903();
      matrix.method_46416(renderX, renderY, 0.0F);
      matrix.method_22905(renderWidth, renderHeight, 1.0F);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      drawTexture(matrix, id, 0, 0, 1.0F, 1.0F, 0.0F, 0.0F, 1, 1, 1, 1, -1);
      RenderSystem.disableBlend();
      matrix.method_22909();
   }

   public static Color applyOpacity(Color color, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity));
   }

   public static int applyOpacity(int color_int, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      Color color = new Color(color_int);
      return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha() * opacity)).getRGB();
   }

   public static void drawTexture(
      class_332 context, class_2960 id, float x, float y, float size, float round, int uvSize, int regionSize, int textureSize, int backgroundColor
   ) {
      drawTexture(context, id, x, y, size, round, uvSize, regionSize, textureSize, backgroundColor, -1);
   }

   public static void drawTexture(
      class_332 context, class_2960 id, float x, float y, float size, float round, int uvSize, int regionSize, int textureSize, int backgroundColor, int color
   ) {
      class_4587 matrix = context.method_51448();
      rectangle.render(ShapeProperties.create(matrix, x, y, size, size).round(round).color(backgroundColor).build());
      if (id != null) {
         float renderX = UiScale.x(x);
         float renderY = UiScale.y(y);
         float renderSize = UiScale.size(size);
         matrix.method_22903();
         matrix.method_46416(renderX, renderY, 0.0F);
         matrix.method_22905(renderSize, renderSize, 1.0F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(772, 773);
         drawTexture(matrix, id, 0, 0, 1.0F, 1.0F, uvSize, uvSize, regionSize, regionSize, textureSize, textureSize, color);
         RenderSystem.disableBlend();
         matrix.method_22909();
      }
   }

   public static void drawSprite(class_4587 matrix, class_1058 sprite, float x, float y, float width, int height) {
      if (width != 0.0F && height != 0) {
         float renderX = UiScale.x(x);
         float renderY = UiScale.y(y);
         float renderWidth = UiScale.size(width);
         float renderHeight = UiScale.size((float)height);
         drawTexturedQuad(
            matrix,
            sprite.method_45852(),
            renderX,
            renderX + renderWidth,
            renderY,
            renderY + renderHeight,
            sprite.method_4594(),
            sprite.method_4577(),
            sprite.method_4593(),
            sprite.method_4575(),
            -1
         );
      }
   }

   public static void drawSprite(class_4587 matrix, class_1058 sprite, float x, float y, float width, int height, int color) {
      if (width != 0.0F && height != 0) {
         float renderX = UiScale.x(x);
         float renderY = UiScale.y(y);
         float renderWidth = UiScale.size(width);
         float renderHeight = UiScale.size((float)height);
         drawTexturedQuad(
            matrix,
            sprite.method_45852(),
            renderX,
            renderX + renderWidth,
            renderY,
            renderY + renderHeight,
            sprite.method_4594(),
            sprite.method_4577(),
            sprite.method_4593(),
            sprite.method_4575(),
            color
         );
      }
   }

   public static void drawTexture(
      class_4587 matrix,
      class_2960 texture,
      int x,
      int y,
      float width,
      float height,
      float u,
      float v,
      int regionWidth,
      int regionHeight,
      int textureWidth,
      int textureHeight,
      int color
   ) {
      drawTexture(matrix, texture, x, x + width, y, y + height, 0.0F, regionWidth, regionHeight, u, v, textureWidth, textureHeight, color);
   }

   public static void drawTexture(
      class_4587 matrix,
      class_2960 texture,
      float x1,
      float x2,
      float y1,
      float y2,
      float z,
      int regionWidth,
      int regionHeight,
      float u,
      float v,
      int textureWidth,
      int textureHeight,
      int color
   ) {
      drawTexturedQuad(
         matrix,
         texture,
         x1,
         x2,
         y1,
         y2,
         (u + 0.0F) / textureWidth,
         (u + regionWidth) / textureWidth,
         (v + 0.0F) / textureHeight,
         (v + regionHeight) / textureHeight,
         color
      );
   }

   public static void drawTexturedQuad(
      class_4587 matrix, class_2960 texture, float x1, float x2, float y1, float y2, float u1, float u2, float v1, float v2, int color
   ) {
      RenderSystem.setShaderTexture(0, texture);
      RenderSystem.setShader(class_10142.field_53880);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      Matrix4f matrix4f = matrix.method_23760().method_23761();
      buffer.method_22918(matrix4f, x1, y1, 0.0F).method_22913(u1, v1).method_39415(color);
      buffer.method_22918(matrix4f, x1, y2, 0.0F).method_22913(u1, v2).method_39415(color);
      buffer.method_22918(matrix4f, x2, y2, 0.0F).method_22913(u2, v2).method_39415(color);
      buffer.method_22918(matrix4f, x2, y1, 0.0F).method_22913(u2, v1).method_39415(color);
      class_286.method_43433(buffer.method_60800());
   }

   public static void drawCircle(class_4587 matrix, float x, float y, float radius, int color) {
      x = UiScale.x(x);
      y = UiScale.y(y);
      radius = UiScale.size(radius);
      int segments = 16;
      float angleStep = (float)((Math.PI * 2) / segments);
      Matrix4f matrix4f = matrix.method_23760().method_23761();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(class_10142.field_53876);
      class_287 buffer = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);

      for (int i = 0; i < segments; i++) {
         float angle1 = i * angleStep;
         float angle2 = (i + 1) * angleStep;
         float x1 = x + radius * (float)Math.cos(angle1);
         float y1 = y + radius * (float)Math.sin(angle1);
         float x2 = x + radius * (float)Math.cos(angle2);
         float y2 = y + radius * (float)Math.sin(angle2);
         buffer.method_22918(matrix4f, x, y, 0.0F).method_39415(color);
         buffer.method_22918(matrix4f, x1, y1, 0.0F).method_39415(color);
         buffer.method_22918(matrix4f, x2, y2, 0.0F).method_39415(color);
         buffer.method_22918(matrix4f, x, y, 0.0F).method_39415(color);
      }

      class_286.method_43433(buffer.method_60800());
      RenderSystem.disableBlend();
   }

   public static void endBuilding(class_287 bb) {
      class_9801 builtBuffer = bb.method_60794();
      if (builtBuffer != null) {
         class_286.method_43433(builtBuffer);
      }
   }

   public static void drawQuad(float x, float y, float width, float height, int color) {
      QUAD.add(
         new Render2D.Quad(
            UiScale.x(x),
            UiScale.y(y),
            UiScale.size(width),
            UiScale.size(height),
            ColorAssist.multAlpha(color, RenderSystem.getShaderColor()[3])
         )
      );
   }

   private Render2D() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public record Quad(float x, float y, float width, float height, int color) {
   }
}
