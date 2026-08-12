package pl.astralvisuals.utils.display.atlasfont.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4588;
import org.joml.Matrix4f;
import pl.astralvisuals.utils.display.atlasfont.providers.ResourceProvider;

public final class MsdfFont {
   private final String name;
   private final class_1044 texture;
   private final FontData.AtlasData atlas;
   private final FontData.MetricsData metrics;
   private final Map<Integer, MsdfGlyph> glyphs;
   private final Map<Integer, Map<Integer, Float>> kernings;

   private MsdfFont(
      String name,
      class_1044 texture,
      FontData.AtlasData atlas,
      FontData.MetricsData metrics,
      Map<Integer, MsdfGlyph> glyphs,
      Map<Integer, Map<Integer, Float>> kernings
   ) {
      this.name = name;
      this.texture = texture;
      this.atlas = atlas;
      this.metrics = metrics;
      this.glyphs = glyphs;
      this.kernings = kernings;
   }

   public int getTextureId() {
      return this.texture.method_4624();
   }

   public void applyGlyphs(Matrix4f matrix, class_4588 consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color) {
      int prevChar = -1;

      for (int i = 0; i < text.length(); i++) {
         int _char = text.charAt(i);
         MsdfGlyph glyph = this.glyphs.get(_char);
         if (glyph != null) {
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
               x += kerning.getOrDefault(_char, 0.0F) * size;
            }

            x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
            prevChar = _char;
         }
      }
   }

   public float getWidth(String text, float size) {
      int prevChar = -1;
      float width = 0.0F;

      for (int i = 0; i < text.length(); i++) {
         int _char = text.charAt(i);
         MsdfGlyph glyph = this.glyphs.get(_char);
         if (glyph != null) {
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
               width += kerning.getOrDefault(_char, 0.0F) * size;
            }

            width += glyph.getWidth(size);
            prevChar = _char;
         }
      }

      return width;
   }

   public String getName() {
      return this.name;
   }

   public FontData.AtlasData getAtlas() {
      return this.atlas;
   }

   public FontData.MetricsData getMetrics() {
      return this.metrics;
   }

   public static MsdfFont.Builder builder() {
      return new MsdfFont.Builder();
   }

   public static class Builder {
      private String name = "?";
      private class_2960 dataIdentifer;
      private class_2960 atlasIdentifier;

      private Builder() {
      }

      public MsdfFont.Builder name(String name) {
         this.name = name;
         return this;
      }

      public MsdfFont.Builder data(String dataFileName) {
         this.dataIdentifer = class_2960.method_60655("mre", "fonts/" + dataFileName + ".json");
         return this;
      }

      public MsdfFont.Builder atlas(String atlasFileName) {
         this.atlasIdentifier = class_2960.method_60655("mre", "fonts/" + atlasFileName + ".png");
         return this;
      }

      public MsdfFont build() {
         FontData data = ResourceProvider.fromJsonToInstance(this.dataIdentifer, FontData.class);
         class_1044 texture = class_310.method_1551().method_1531().method_4619(this.atlasIdentifier);
         if (data == null) {
            throw new RuntimeException(
               "Failed to read font data file: "
                  + this.dataIdentifer.toString()
                  + "; Are you sure this is json file? Try to check the correctness of its syntax."
            );
         } else {
            RenderSystem.recordRenderCall(() -> texture.method_4527(true, false));
            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs()
               .stream()
               .collect(Collectors.toMap(glyphData -> glyphData.unicode(), glyphData -> new MsdfGlyph(glyphData, aWidth, aHeight)));
            Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();
            data.kernings().forEach(kerning -> {
               Map<Integer, Float> map = kernings.get(kerning.leftChar());
               if (map == null) {
                  map = new HashMap<>();
                  kernings.put(kerning.leftChar(), map);
               }

               map.put(kerning.rightChar(), kerning.advance());
            });
            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
         }
      }
   }
}
