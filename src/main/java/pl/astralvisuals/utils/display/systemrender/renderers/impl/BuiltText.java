package pl.astralvisuals.utils.display.systemrender.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_10149;
import net.minecraft.class_10156;
import net.minecraft.class_286;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_5944;
import net.minecraft.class_293.class_5596;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.atlasfont.msdf.MsdfFont;
import pl.astralvisuals.utils.display.atlasfont.providers.ColorProvider;
import pl.astralvisuals.utils.display.atlasfont.providers.ResourceProvider;
import pl.astralvisuals.utils.display.systemrender.renderers.IRenderer;

public record BuiltText(
   MsdfFont font, String text, float size, float thickness, int color, float smoothness, float spacing, int outlineColor, float outlineThickness
) implements IRenderer {
   private static final class_10156 MSDF_FONT_SHADER_KEY = new class_10156(
      ResourceProvider.getShaderIdentifier("msdf_font"), class_290.field_1575, class_10149.field_53930
   );

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.font.getTextureId());
      boolean outlineEnabled = this.outlineThickness > 0.0F;
      class_5944 shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
      shader.method_34582("Range").method_1251(this.font.getAtlas().range());
      shader.method_34582("Thickness").method_1251(this.thickness);
      shader.method_34582("Smoothness").method_1251(this.smoothness);
      shader.method_34582("Outline").method_35649(outlineEnabled ? 1 : 0);
      if (outlineEnabled) {
         shader.method_34582("OutlineThickness").method_1251(this.outlineThickness);
         float[] outlineComponents = ColorProvider.normalize(this.outlineColor);
         shader.method_34582("OutlineColor").method_35657(outlineComponents[0], outlineComponents[1], outlineComponents[2], outlineComponents[3]);
      }

      class_287 builder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
      this.font
         .applyGlyphs(
            matrix,
            builder,
            this.text,
            this.size,
            (this.thickness + this.outlineThickness * 0.5F) * 0.5F * this.size,
            this.spacing,
            x,
            y + this.font.getMetrics().baselineHeight() * this.size,
            z,
            this.color
         );
      class_286.method_43433(builder.method_60800());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
