package pl.astralvisuals.display.hud;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import net.minecraft.class_1011;

/** Regression check for artwork work that previously ran on Minecraft's render thread. */
public final class MediaPlayerPerformanceTest {
   private static final int SOURCE_SIZE = 1600;
   private static final int ITERATIONS = 50;

   private MediaPlayerPerformanceTest() {
   }

   public static void main(String[] args) throws Exception {
      byte[] artwork = createArtwork();
      long totalNanos = 0L;
      long maxNanos = 0L;

      for (int iteration = 0; iteration < ITERATIONS; iteration++) {
         long startedAt = System.nanoTime();
         try (class_1011 image = MediaPlayer.prepareArtwork(artwork)) {
            require(image.method_4307() == 96, "Unexpected artwork width: " + image.method_4307());
            require(image.method_4323() == 96, "Unexpected artwork height: " + image.method_4323());
         }
         long elapsed = System.nanoTime() - startedAt;
         totalNanos += elapsed;
         maxNanos = Math.max(maxNanos, elapsed);
      }

      double averageMs = totalNanos / (double)ITERATIONS / 1_000_000.0;
      double maximumMs = maxNanos / 1_000_000.0;
      System.out.printf(
         "MEDIA PLAYER PERFORMANCE TEST PASSED: %d off-thread decodes, %.2f ms average, %.2f ms max%n",
         ITERATIONS,
         averageMs,
         maximumMs
      );
   }

   private static byte[] createArtwork() throws Exception {
      BufferedImage image = new BufferedImage(SOURCE_SIZE, SOURCE_SIZE, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = image.createGraphics();
      try {
         graphics.setPaint(new GradientPaint(0, 0, new Color(14, 20, 34), SOURCE_SIZE, SOURCE_SIZE, new Color(116, 43, 210)));
         graphics.fillRect(0, 0, SOURCE_SIZE, SOURCE_SIZE);
      } finally {
         graphics.dispose();
      }
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      require(ImageIO.write(image, "png", output), "PNG writer is unavailable");
      return output.toByteArray();
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
