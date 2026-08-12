package pl.astralvisuals.utils.math.calc;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_9848;
import org.joml.Vector3d;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.scale.UiScale;

public final class Calculate implements QuickImports {
   public static double PI2 = Math.PI * 2;

   public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
      return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
   }

   public static float clamp(float num, float min, float max) {
      return num < min ? min : Math.min(num, max);
   }

   public static double computeGcd() {
      return Math.pow((Double)mc.field_1690.method_42495().method_41753() * 0.6 + 0.2, 3.0) * 1.2;
   }

   public static int getRandom(int min, int max) {
      return (int)getRandom((float)min, max + 1.0F);
   }

   public static float getRandom(float min, float max) {
      return (float)getRandom((double)min, (double)max);
   }

   public static double getRandom(double min, double max) {
      if (min == max) {
         return min;
      } else {
         if (min > max) {
            double d = min;
            min = max;
            max = d;
         }

         return ThreadLocalRandom.current().nextDouble(min, max);
      }
   }

   public static void scale(class_4587 stack, float x, float y, float scale, Runnable data) {
      if (scale != 1.0F) {
         float scale2 = 0.5F + scale / 2.0F;
         x = UiScale.x(x);
         y = UiScale.y(y);
         stack.method_22903();
         stack.method_46416(x, y, 0.0F);
         stack.method_22905(scale2, scale2, 1.0F);
         stack.method_46416(-x, -y, 0.0F);
         setAlpha(scale, data);
         stack.method_22909();
      } else {
         data.run();
      }
   }

   public static void scale(class_4587 stack, float x, float y, float scaleX, float scaleY, Runnable data) {
      float sumScale = scaleX * scaleY;
      if (sumScale != 1.0F) {
         x = UiScale.x(x);
         y = UiScale.y(y);
         stack.method_22903();
         stack.method_46416(x, y, 0.0F);
         stack.method_22905(scaleX, scaleY, 1.0F);
         stack.method_46416(-x, -y, 0.0F);
         setAlpha(sumScale, data);
         stack.method_22909();
      } else {
         data.run();
      }
   }

   public static float textScrolling(float textWidth) {
      int speed = (int)(textWidth * 75.0F);
      return (float)class_3532.method_15350(System.currentTimeMillis() % speed * Math.PI / speed, 0.0, 1.0) * textWidth;
   }

   public static void setAlpha(float alpha, Runnable data) {
      setColor(1.0F, 1.0F, 1.0F, alpha, data);
   }

   public static void setColor(float red, float green, float blue, float alpha, Runnable data) {
      RenderSystem.setShaderColor(
         class_3532.method_15363(red, 0.0F, 1.0F),
         class_3532.method_15363(green, 0.0F, 1.0F),
         class_3532.method_15363(blue, 0.0F, 1.0F),
         class_3532.method_15363(alpha, 0.0F, 1.0F)
      );
      data.run();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static double round(double num, double increment) {
      double rounded = Math.round(num / increment) * increment;
      return Math.round(rounded * 100.0) / 100.0;
   }

   public static int floorNearestMulN(int x, int n) {
      return n * (int)Math.floor((double)x / n);
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

   public static int applyOpacity(int color, float opacity) {
      return class_9848.method_61324((int)(getAlpha(color) * opacity / 255.0F), getRed(color), getGreen(color), getBlue(color));
   }

   public static class_243 cosSin(int i, int size, double width) {
      int index = Math.min(i, size);
      float cos = (float)(Math.cos(index * PI2 / size) * width);
      float sin = (float)(-Math.sin(index * PI2 / size) * width);
      return new class_243(cos, 0.0, sin);
   }

   public static double absSinAnimation(double input) {
      return Math.abs(1.0 + Math.sin(input)) / 2.0;
   }

   public static Vector3d interpolate(Vector3d prevPos, Vector3d pos) {
      return new Vector3d(interpolate(prevPos.x, pos.x), interpolate(prevPos.y, pos.y), interpolate(prevPos.z, pos.z));
   }

   public static float interpolate(float prev, float to, float value) {
      return prev + (to - prev) * value;
   }

   public static class_243 interpolate(class_243 prevPos, class_243 pos) {
      return new class_243(
         interpolate(prevPos.field_1352, pos.field_1352), interpolate(prevPos.field_1351, pos.field_1351), interpolate(prevPos.field_1350, pos.field_1350)
      );
   }

   public static class_243 interpolate(class_1297 entity) {
      return entity == null
         ? class_243.field_1353
         : new class_243(
            interpolate(entity.field_6014, entity.method_23317()),
            interpolate(entity.field_6036, entity.method_23318()),
            interpolate(entity.field_5969, entity.method_23321())
         );
   }

   public static float interpolate(float prev, float orig) {
      return class_3532.method_16439(tickCounter.method_60637(false), prev, orig);
   }

   public static double interpolate(double prev, double orig) {
      return class_3532.method_16436(tickCounter.method_60637(false), prev, orig);
   }

   public static float interpolateSmooth(double smooth, float prev, float orig) {
      return (float)class_3532.method_16436(tickCounter.method_60638() / smooth, prev, orig);
   }

   private Calculate() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
