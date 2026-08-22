package pl.astralvisuals.utils.display.font.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.class_1011;
import net.minecraft.class_1043;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_9848;
import net.minecraft.class_1011.class_1012;
import org.lwjgl.system.MemoryUtil;

public class GlyphMap {
   private final Char2ObjectArrayMap<Glyph> glyphs = new Char2ObjectArrayMap();
   private final char fromIncl;
   private final char toExcl;
   private final Font font;
   public final class_2960 bindToTexture;
   private final int pixelPadding;
   public int width;
   public int height;
   private boolean generated = false;

   public GlyphMap(char from, char to, Font font, class_2960 identifier, int padding) {
      this.fromIncl = from;
      this.toExcl = to;
      this.font = font;
      this.bindToTexture = identifier;
      this.pixelPadding = padding;
   }

   public Glyph getGlyph(char c) {
      if (!this.generated) {
         this.generate();
      }

      return (Glyph)this.glyphs.get(c);
   }

   public boolean contains(char c) {
      return c >= this.fromIncl && c < this.toExcl;
   }

   private Font getFontForGlyph(char c) {
      return this.font.canDisplay(c)
         ? this.font
         : Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
            .filter(f -> f.canDisplay(c))
            .map(f -> new Font(f.getFontName(), this.font.getStyle(), this.font.getSize()))
            .findFirst()
            .orElse(new Font("SansSerif", this.font.getStyle(), this.font.getSize()));
   }

   public void generate() {
      if (!this.generated) {
         int range = this.toExcl - this.fromIncl - 1;
         int charsVert = (int)(Math.ceil(Math.sqrt(range)) * 1.5);
         this.glyphs.clear();
         int generatedChars = 0;
         int charNX = 0;
         int maxX = 0;
         int maxY = 0;
         int currentX = 0;
         int currentY = 0;
         int currentRowMaxY = 0;
         List<Glyph> glyphs1 = new ArrayList<>();
         AffineTransform affineTransform = new AffineTransform();

         for (FontRenderContext fontRenderContext = new FontRenderContext(affineTransform, true, false); generatedChars <= range; charNX++) {
            char currentChar = (char)(this.fromIncl + generatedChars);
            Font font = this.getFontForGlyph(currentChar);
            Rectangle2D stringBounds = font.getStringBounds(String.valueOf(currentChar), fontRenderContext);
            int width = (int)Math.ceil(stringBounds.getWidth());
            int height = (int)Math.ceil(stringBounds.getHeight());
            generatedChars++;
            maxX = Math.max(maxX, currentX + width);
            maxY = Math.max(maxY, currentY + height);
            if (charNX >= charsVert) {
               currentX = 0;
               currentY += currentRowMaxY + this.pixelPadding;
               charNX = 0;
               currentRowMaxY = 0;
            }

            currentRowMaxY = Math.max(currentRowMaxY, height);
            glyphs1.add(new Glyph(currentX, currentY, width, height, currentChar, this));
            currentX += width + this.pixelPadding;
         }

         BufferedImage bufferedImage = new BufferedImage(Math.max(maxX + this.pixelPadding, 1), Math.max(maxY + this.pixelPadding, 1), 2);
         this.width = bufferedImage.getWidth();
         this.height = bufferedImage.getHeight();
         Graphics2D g2d = bufferedImage.createGraphics();
         g2d.setColor(new Color(255, 255, 255, 0));
         g2d.fillRect(0, 0, this.width, this.height);
         g2d.setColor(Color.WHITE);
         g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
         g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

         for (Glyph glyph : glyphs1) {
            g2d.setFont(this.getFontForGlyph(glyph.value()));
            FontMetrics fontMetrics = g2d.getFontMetrics();
            g2d.drawString(String.valueOf(glyph.value()), glyph.u(), glyph.v() + fontMetrics.getAscent());
            this.glyphs.put(glyph.value(), glyph);
         }

         registerBufferedImageTexture(this.bindToTexture, bufferedImage);
         this.generated = true;
      }
   }

   public static void registerBufferedImageTexture(class_2960 textureIdentifier, BufferedImage inputImage) {
      try {
         int imageWidth = inputImage.getWidth();
         int imageHeight = inputImage.getHeight();
         class_1011 nativeImage = new class_1011(class_1012.field_4997, imageWidth, imageHeight, false);
         IntBuffer buffer = MemoryUtil.memIntBuffer(nativeImage.field_4988, nativeImage.method_4307() * nativeImage.method_4323());
         WritableRaster raster = inputImage.getRaster();
         ColorModel colorModel = inputImage.getColorModel();
         Object data = createDataArrayBasedOnRaster(raster);

         for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
               raster.getDataElements(x, y, data);
               int alpha = colorModel.getAlpha(data);
               int red = colorModel.getRed(data);
               int green = colorModel.getGreen(data);
               int blue = colorModel.getBlue(data);
               buffer.put(class_9848.method_61324(alpha, blue, green, red));
            }
         }

         class_1043 texture = new class_1043(nativeImage);
         // HUD and ClickGUI support fractional scaling. Nearest-neighbour
         // sampling makes the glyph atlas visibly pixelated at those scales.
         texture.method_4527(true, false);
         texture.method_4524();
         if (RenderSystem.isOnRenderThread()) {
            class_310.method_1551().method_1531().method_4616(textureIdentifier, texture);
         } else {
            RenderSystem.recordRenderCall(() -> class_310.method_1551().method_1531().method_4616(textureIdentifier, texture));
         }
      } catch (Throwable var15) {
         throw var15;
      }
   }

   private static Object createDataArrayBasedOnRaster(WritableRaster raster) {
      return switch (raster.getDataBuffer().getDataType()) {
         case 0 -> new byte[raster.getNumDataElements()];
         case 1 -> new short[raster.getNumDataElements()];
         default -> throw new IllegalArgumentException("Unsupported data buffer type: " + raster.getDataBuffer().getDataType());
         case 3 -> new int[raster.getNumDataElements()];
         case 4 -> new float[raster.getNumDataElements()];
         case 5 -> new double[raster.getNumDataElements()];
      };
   }
}
