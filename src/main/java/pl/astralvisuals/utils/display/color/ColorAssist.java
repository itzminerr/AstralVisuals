package pl.astralvisuals.utils.display.color;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import net.minecraft.class_3532;
import net.minecraft.class_9848;
import pl.astralvisuals.features.impl.render.Interface;
import pl.astralvisuals.utils.math.calc.Calculate;

public final class ColorAssist {
   public static final int green = new Color(64, 255, 64).getRGB();
   public static final int yellow = new Color(255, 255, 64).getRGB();
   public static final int orange = new Color(255, 128, 32).getRGB();
   public static final int red = new Color(255, 64, 64).getRGB();
   private static final long CACHE_EXPIRATION_TIME = 60000L;
   private static final ConcurrentHashMap<ColorAssist.ColorKey, ColorAssist.CacheEntry> colorCache = new ConcurrentHashMap<>();
   private static final ScheduledExecutorService cacheCleaner = Executors.newScheduledThreadPool(1, runnable -> {
      Thread thread = new Thread(runnable, "AstralVisuals-ColorCache");
      thread.setDaemon(true);
      return thread;
   });
   private static final DelayQueue<ColorAssist.CacheEntry> cleanupQueue = new DelayQueue<>();
   public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");
   public static Char2IntArrayMap colorCodes = new Char2IntArrayMap() {
      {
         this.put('0', 0);
         this.put('1', 170);
         this.put('2', 43520);
         this.put('3', 43690);
         this.put('4', 11141120);
         this.put('5', 11141290);
         this.put('6', 16755200);
         this.put('7', 11184810);
         this.put('8', 5592405);
         this.put('9', 5592575);
         this.put('A', 5635925);
         this.put('B', 5636095);
         this.put('C', 16733525);
         this.put('D', 16733695);
         this.put('E', 16777045);
         this.put('F', 16777215);
      }
   };
   public static final int RED = getColor(255, 0, 0);
   public static final int GREEN = getColor(0, 255, 0);
   public static final int BLUE = getColor(0, 0, 255);
   public static final int YELLOW = getColor(255, 255, 0);
   public static final int WHITE = getColor(255);
   public static final int BLACK = getColor(0);
   public static final int HALF_BLACK = getColor(0, 0.5F);
   public static final int LIGHT_RED = getColor(255, 85, 85);

   public static int colorForRectsCustom$() {
      return new Color(30, 30, 34, 255).getRGB();
   }

   public static int colorForRectsBlack$() {
      return new Color(26, 26, 26, 255).getRGB();
   }

   public static int colorForTextWhite$() {
      return new Color(255, 255, 255, 255).getRGB();
   }

   public static int colorForTextCustom$() {
      return new Color(210, 210, 218, 255).getRGB();
   }

   public static int red(int c) {
      return c >> 16 & 0xFF;
   }

   public static int green(int c) {
      return c >> 8 & 0xFF;
   }

   public static int blue(int c) {
      return c & 0xFF;
   }

   public static int alpha(int c) {
      return c >> 24 & 0xFF;
   }

   public static float redf(int c) {
      return red(c) / 255.0F;
   }

   public static float greenf(int c) {
      return green(c) / 255.0F;
   }

   public static float bluef(int c) {
      return blue(c) / 255.0F;
   }

   public static float alphaf(int c) {
      return alpha(c) / 255.0F;
   }

   public static int[] getRGBA(int c) {
      return new int[]{red(c), green(c), blue(c), alpha(c)};
   }

   public static int[] getRGB(int c) {
      return new int[]{red(c), green(c), blue(c)};
   }

   public static float[] getRGBAf(int c) {
      return new float[]{redf(c), greenf(c), bluef(c), alphaf(c)};
   }

   public static float[] getRGBf(int c) {
      return new float[]{redf(c), greenf(c), bluef(c)};
   }

   public static int getColor(float red, float green, float blue, float alpha) {
      return getColor(Math.round(red * 255.0F), Math.round(green * 255.0F), Math.round(blue * 255.0F), Math.round(alpha * 255.0F));
   }

   public static int getColor(int red, int green, int blue, float alpha) {
      return getColor(red, green, blue, Math.round(alpha * 255.0F));
   }

   public static int getColor(float red, float green, float blue) {
      return getColor(red, green, blue, 1.0F);
   }

   public static int getColor(int brightness, int alpha) {
      return getColor(brightness, brightness, brightness, alpha);
   }

   public static int getColor(int brightness, float alpha) {
      return getColor(brightness, Math.round(alpha * 255.0F));
   }

   public static int getColor(int brightness) {
      return getColor(brightness, brightness, brightness);
   }

   public static int replAlpha(int color, int alpha) {
      return getColor(red(color), green(color), blue(color), alpha);
   }

   public static int replAlpha(int color, float alpha) {
      return getColor(red(color), green(color), blue(color), alpha);
   }

   public static int multAlpha(int color, float percent01) {
      return getColor(red(color), green(color), blue(color), Math.round(alpha(color) * percent01));
   }

   public static int applyOpacity(int hex, int percent) {
      return applyOpacity(hex, 2.55F * Math.min(percent, 100));
   }

   public static int applyOpacity(int hex, float opacity) {
      return class_9848.method_61324(
         (int)(class_9848.method_61320(hex) * (opacity / 255.0F)), class_9848.method_61327(hex), class_9848.method_61329(hex), class_9848.method_61331(hex)
      );
   }

   public static int lerp(float value, int from, int to) {
      return class_9848.method_61319(value, from, to);
   }

   public static int pixelColor(int x, int y) {
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
      GL11.glReadPixels(x, y, 1, 1, 6408, 5121, byteBuffer);
      return class_9848.method_61323(getRed(byteBuffer.get()), getGreen(byteBuffer.get()), getBlue(byteBuffer.get()));
   }

   public static int calculateHuyDegrees(int divisor, int offset) {
      long currentTime = System.currentTimeMillis();
      long calculatedValue = (currentTime / divisor + offset) % 360L;
      return (int)calculatedValue;
   }

   public static int reAlphaInt(int color, int alpha) {
      return Math.clamp((long)alpha, 0, 255) << 24 | color & 16777215;
   }

   public static int astolfo(int speed, int index, float saturation, float brightness, float alpha) {
      float hueStep = 90.0F;
      float basaHuy = calculateHuyDegrees(speed, index);
      float huy = (basaHuy + index * hueStep) % 360.0F;
      huy /= 360.0F;
      saturation = Math.clamp(saturation, 0.0F, 1.0F);
      brightness = Math.clamp(brightness, 0.0F, 1.0F);
      int rgb = Color.HSBtoRGB(huy, saturation, brightness);
      int Ialpha = Math.max(0, Math.min(255, (int)(alpha * 255.0F)));
      return reAlphaInt(rgb, Ialpha);
   }

   public static int rgb(int r, int g, int b) {
      return 0xFF000000 | r << 16 | g << 8 | b;
   }

   public static void setAlphaColor(int color, float alpha) {
      float red = (color >> 16 & 0xFF) / 255.0F;
      float green = (color >> 8 & 0xFF) / 255.0F;
      float blue = (color & 0xFF) / 255.0F;
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }

   public static void setColor(int color) {
      setAlphaColor(color, (color >> 24 & 0xFF) / 255.0F);
   }

   public static int toColor(String hexColor) {
      int argb = Integer.parseInt(hexColor.substring(1), 16);
      return setAlpha(argb, 255);
   }

   public static int setAlpha(int color, int alpha) {
      return color & 16777215 | alpha << 24;
   }

   public static int gradient(int start, int end, int index, int speed) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      int color = interpolate(start, end, Math.clamp(angle / 180.0F - 1.0F, 0.0F, 1.0F));
      float[] hs = rgba(color);
      float[] hsb = Color.RGBtoHSB((int)(hs[0] * 255.0F), (int)(hs[1] * 255.0F), (int)(hs[2] * 255.0F), null);
      hsb[1] *= 1.5F;
      hsb[1] = Math.min(hsb[1], 1.0F);
      return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
   }

   public static int gradient(int speed, int index, int... colors) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = (angle > 180 ? 360 - angle : angle) + 180;
      int colorIndex = (int)(angle / 360.0F * colors.length);
      if (colorIndex == colors.length) {
         colorIndex--;
      }

      int color1 = colors[colorIndex];
      int color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
      return interpolate(color1, color2, angle / 360.0F * colors.length - colorIndex);
   }

   public static int interpolateColor(int color1, int color2, float amount) {
      amount = Math.min(1.0F, Math.max(0.0F, amount));
      int red1 = getRed(color1);
      int green1 = getGreen(color1);
      int blue1 = getBlue(color1);
      int alpha1 = getAlpha(color1);
      int red2 = getRed(color2);
      int green2 = getGreen(color2);
      int blue2 = getBlue(color2);
      int alpha2 = getAlpha(color2);
      int interpolatedRed = interpolateInt(red1, red2, amount);
      int interpolatedGreen = interpolateInt(green1, green2, amount);
      int interpolatedBlue = interpolateInt(blue1, blue2, amount);
      int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);
      return interpolatedAlpha << 24 | interpolatedRed << 16 | interpolatedGreen << 8 | interpolatedBlue;
   }

   public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
      return oldValue + (newValue - oldValue) * interpolationValue;
   }

   public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
      return interpolateD(oldValue, newValue, (float)interpolationValue).intValue();
   }

   public static int getRed(int hex) {
      return hex >> 16 & 0xFF;
   }

   public static int getGreen(int hex) {
      return hex >> 8 & 0xFF;
   }

   public static int getBlue(int hex) {
      return hex & 0xFF;
   }

   public static int getAlpha(int hex) {
      return hex >> 24 & 0xFF;
   }

   public static int multColor(int colorStart, int colorEnd, float progress) {
      return getColor(
         Math.round(red(colorStart) * (redf(colorEnd) * progress)),
         Math.round(green(colorStart) * (greenf(colorEnd) * progress)),
         Math.round(blue(colorStart) * (bluef(colorEnd) * progress)),
         Math.round(alpha(colorStart) * (alphaf(colorEnd) * progress))
      );
   }

   public static int multRed(int colorStart, int colorEnd, float progress) {
      return getColor(
         Math.round(red(colorStart) * (redf(colorEnd) * progress)),
         Math.round(green(colorStart) * (greenf(colorEnd) * progress)),
         Math.round(blue(colorStart) * (bluef(colorEnd) * progress)),
         Math.round(alpha(colorStart) * (alphaf(colorEnd) * progress))
      );
   }

   public static int multDark(int color, float percent01) {
      return getColor(Math.round(red(color) * percent01), Math.round(green(color) * percent01), Math.round(blue(color) * percent01), alpha(color));
   }

   public static int multBright(int color, float percent01) {
      return getColor(
         Math.min(255, Math.round(red(color) / percent01)),
         Math.min(255, Math.round(green(color) / percent01)),
         Math.min(255, Math.round(blue(color) / percent01)),
         alpha(color)
      );
   }

   public static int overCol(int color1, int color2, float percent01) {
      float percent = class_3532.method_15363(percent01, 0.0F, 1.0F);
      return getColor(
         class_3532.method_48781(percent, red(color1), red(color2)),
         class_3532.method_48781(percent, green(color1), green(color2)),
         class_3532.method_48781(percent, blue(color1), blue(color2)),
         class_3532.method_48781(percent, alpha(color1), alpha(color2))
      );
   }

   public static Vector4i multRedAndAlpha(Vector4i color, float red, float alpha) {
      return new Vector4i(
         multRedAndAlpha(color.x, red, alpha), multRedAndAlpha(color.y, red, alpha), multRedAndAlpha(color.w, red, alpha), multRedAndAlpha(color.z, red, alpha)
      );
   }

   public static int multRedAndAlpha(int color, float red, float alpha) {
      return getColor(red(color), Math.min(255, Math.round(green(color) / red)), Math.min(255, Math.round(blue(color) / red)), Math.round(alpha(color) * alpha));
   }

   public static int multRed(int color, float percent01) {
      return getColor(red(color), Math.min(255, Math.round(green(color) / percent01)), Math.min(255, Math.round(blue(color) / percent01)), alpha(color));
   }

   public static int multGreen(int color, float percent01) {
      return getColor(Math.min(255, Math.round(green(color) / percent01)), green(color), Math.min(255, Math.round(blue(color) / percent01)), alpha(color));
   }

   public static int rgba(int red, int green, int blue, int alpha) {
      return getColor(red, green, blue, alpha);
   }

   public static int[] genGradientForText(int color1, int color2, int length) {
      int[] gradient = new int[length];

      for (int i = 0; i < length; i++) {
         float pc = (float)i / (length - 1);
         gradient[i] = overCol(color1, color2, pc);
      }

      return gradient;
   }

   public static float[] rgba(int color) {
      return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F};
   }

   public static int interpolate(int start, int end, float value) {
      float[] startColor = rgba(start);
      float[] endColor = rgba(end);
      return rgba(
         (int)Calculate.interpolate(startColor[0] * 255.0F, endColor[0] * 255.0F, value),
         (int)Calculate.interpolate(startColor[1] * 255.0F, endColor[1] * 255.0F, value),
         (int)Calculate.interpolate(startColor[2] * 255.0F, endColor[2] * 255.0F, value),
         (int)Calculate.interpolate(startColor[3] * 255.0F, endColor[3] * 255.0F, value)
      );
   }

   public static int rainbow(int speed, int index, float saturation, float brightness, float opacity) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      float hue = angle / 360.0F;
      int color = Color.HSBtoRGB(hue, saturation, brightness);
      return getColor(red(color), green(color), blue(color), Math.round(opacity * 255.0F));
   }

   public static int fade(int speed, int index, int first, int second) {
      int angle = (int)((System.currentTimeMillis() / speed + index) % 360L);
      angle = angle >= 180 ? 360 - angle : angle;
      return overCol(first, second, angle / 180.0F);
   }

   public static int rgbaFloat(float r, float g, float b, float a) {
      return (int)(class_3532.method_15363(a, 0.0F, 1.0F) * 255.0F) << 24
         | (int)(class_3532.method_15363(r, 0.0F, 1.0F) * 255.0F) << 16
         | (int)(class_3532.method_15363(g, 0.0F, 1.0F) * 255.0F) << 8
         | (int)(class_3532.method_15363(b, 0.0F, 1.0F) * 255.0F);
   }

   public static int fade(int index) {
      new Color(getClientColor());
      return fade(8, index, getClientColor(), -1);
   }

   public static Vector4i roundClientColor(float alpha) {
      int color = getColor(225, 225, 255, 255);
      return new Vector4i(multAlpha(color, alpha), multAlpha(color, alpha), multAlpha(color, alpha), multAlpha(color, alpha));
   }

   public static int getColor(int red, int green, int blue, int alpha) {
      ColorAssist.ColorKey key = new ColorAssist.ColorKey(red, green, blue, alpha);
      ColorAssist.CacheEntry cacheEntry = colorCache.computeIfAbsent(key, k -> {
         ColorAssist.CacheEntry newEntry = new ColorAssist.CacheEntry(k, computeColor(red, green, blue, alpha), 60000L);
         cleanupQueue.offer(newEntry);
         return newEntry;
      });
      return cacheEntry.getColor();
   }

   public static int getColor(int red, int green, int blue) {
      return getColor(red, green, blue, 255);
   }

   private static int computeColor(int red, int green, int blue, int alpha) {
      return class_3532.method_15340(alpha, 0, 255) << 24
         | class_3532.method_15340(red, 0, 255) << 16
         | class_3532.method_15340(green, 0, 255) << 8
         | class_3532.method_15340(blue, 0, 255);
   }

   private static String generateKey(int red, int green, int blue, int alpha) {
      return red + "," + green + "," + blue + "," + alpha;
   }

   public static String formatting(int color) {
      return "⏏" + color + "⏏";
   }

   public static String removeFormatting(String text) {
      return text != null && !text.isEmpty() ? FORMATTING_CODE_PATTERN.matcher(text).replaceAll("") : null;
   }

   public static int getMainGuiColor() {
      return new Color(1579037).getRGB();
   }

   public static int getGuiRectColor(float alpha) {
      return multAlpha(new Color(1710623).getRGB(), alpha);
   }

   public static int getGuiRectColor2(float alpha) {
      return multAlpha(new Color(1973798).getRGB(), alpha);
   }

   public static int getRect(float alpha) {
      return multAlpha(new Color(1579036).getRGB(), alpha);
   }

   public static int getRectDarker(float alpha) {
      return multAlpha(new Color(1579038).getRGB(), alpha);
   }

   public static int getText(float alpha) {
      return multAlpha(getText(), alpha);
   }

   public static int getText() {
      return new Color(15132390).getRGB();
   }

   public static int getText2() {
      return new Color(175, 175, 175, 255).getRGB();
   }

   public static int getClientColor() {
      return Interface.getInstance().colorSetting.getColor();
   }

   public static int getClientColor(float alpha) {
      return multAlpha(getClientColor(), alpha);
   }

   public static int getFriendColor() {
      return new Color(5635925).getRGB();
   }

   public static int getOutline(float alpha, float bright) {
      return multBright(multAlpha(getOutline(), alpha), bright);
   }

   public static int getOutline(float alpha) {
      return multAlpha(getOutline(), alpha);
   }

   public static int getOutline() {
      return new Color(369098751, true).getRGB();
   }

   private ColorAssist() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   static {
      cacheCleaner.scheduleWithFixedDelay(() -> {
         for (ColorAssist.CacheEntry entry = cleanupQueue.poll(); entry != null; entry = cleanupQueue.poll()) {
            if (entry.isExpired()) {
               colorCache.remove(entry.getKey());
            }
         }
      }, 0L, 1L, TimeUnit.SECONDS);
   }

   private static class CacheEntry implements Delayed {
      private final ColorAssist.ColorKey key;
      private final int color;
      private final long expirationTime;

      CacheEntry(ColorAssist.ColorKey key, int color, long ttl) {
         this.key = key;
         this.color = color;
         this.expirationTime = System.currentTimeMillis() + ttl;
      }

      @Override
      public long getDelay(TimeUnit unit) {
         long delay = this.expirationTime - System.currentTimeMillis();
         return unit.convert(delay, TimeUnit.MILLISECONDS);
      }

      public int compareTo(Delayed other) {
         return other instanceof ColorAssist.CacheEntry ? Long.compare(this.expirationTime, ((ColorAssist.CacheEntry)other).expirationTime) : 0;
      }

      public boolean isExpired() {
         return System.currentTimeMillis() > this.expirationTime;
      }

      public ColorAssist.ColorKey getKey() {
         return this.key;
      }

      public int getColor() {
         return this.color;
      }

      public long getExpirationTime() {
         return this.expirationTime;
      }
   }

   private static class ColorKey {
      final int red;
      final int green;
      final int blue;
      final int alpha;

      public int getRed() {
         return this.red;
      }

      public int getGreen() {
         return this.green;
      }

      public int getBlue() {
         return this.blue;
      }

      public int getAlpha() {
         return this.alpha;
      }

      public ColorKey(int red, int green, int blue, int alpha) {
         this.red = red;
         this.green = green;
         this.blue = blue;
         this.alpha = alpha;
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof ColorAssist.ColorKey other)) {
            return false;
         } else if (!other.canEqual(this)) {
            return false;
         } else if (this.getRed() != other.getRed()) {
            return false;
         } else if (this.getGreen() != other.getGreen()) {
            return false;
         } else {
            return this.getBlue() != other.getBlue() ? false : this.getAlpha() == other.getAlpha();
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof ColorAssist.ColorKey;
      }

      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         result = result * 59 + this.getRed();
         result = result * 59 + this.getGreen();
         result = result * 59 + this.getBlue();
         return result * 59 + this.getAlpha();
      }
   }

   public static class IntColor {
      public static float[] rgb(int color) {
         return new float[]{(color >> 16 & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color & 0xFF) / 255.0F, (color >> 24 & 0xFF) / 255.0F};
      }

      public static int rgba(int r, int g, int b, int a) {
         return a << 24 | r << 16 | g << 8 | b;
      }

      public static int rgb(int r, int g, int b) {
         return 0xFF000000 | r << 16 | g << 8 | b;
      }

      public static int getRed(int hex) {
         return hex >> 16 & 0xFF;
      }

      public static int getGreen(int hex) {
         return hex >> 8 & 0xFF;
      }

      public static int getBlue(int hex) {
         return hex & 0xFF;
      }

      public static int getAlpha(int hex) {
         return hex >> 24 & 0xFF;
      }
   }
}
