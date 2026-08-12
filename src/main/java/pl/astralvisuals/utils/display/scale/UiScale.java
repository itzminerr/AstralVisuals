package pl.astralvisuals.utils.display.scale;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Applies UI scale to render primitives before their geometry is built.
 * This keeps text and shader shapes sharp instead of stretching a completed frame.
 */
public final class UiScale {
   private static final ThreadLocal<Deque<Transform>> TRANSFORMS = ThreadLocal.withInitial(ArrayDeque::new);

   public static void render(float scale, float pivotX, float pivotY, Runnable renderer) {
      if (Math.abs(scale - 1.0F) < 0.0001F) {
         renderer.run();
         return;
      }

      Deque<Transform> transforms = TRANSFORMS.get();
      transforms.push(new Transform(scale, pivotX, pivotY));

      try {
         renderer.run();
      } finally {
         transforms.pop();
         if (transforms.isEmpty()) {
            TRANSFORMS.remove();
         }
      }
   }

   public static float x(float value) {
      float result = value;
      for (Transform transform : TRANSFORMS.get()) {
         result = transform.pivotX + (result - transform.pivotX) * transform.scale;
      }

      return result;
   }

   public static double x(double value) {
      double result = value;
      for (Transform transform : TRANSFORMS.get()) {
         result = transform.pivotX + (result - transform.pivotX) * transform.scale;
      }

      return result;
   }

   public static float y(float value) {
      float result = value;
      for (Transform transform : TRANSFORMS.get()) {
         result = transform.pivotY + (result - transform.pivotY) * transform.scale;
      }

      return result;
   }

   public static double y(double value) {
      double result = value;
      for (Transform transform : TRANSFORMS.get()) {
         result = transform.pivotY + (result - transform.pivotY) * transform.scale;
      }

      return result;
   }

   public static float size(float value) {
      return value * scale();
   }

   public static double size(double value) {
      return value * scale();
   }

   public static float scale() {
      float result = 1.0F;
      for (Transform transform : TRANSFORMS.get()) {
         result *= transform.scale;
      }

      return result;
   }

   public static boolean active() {
      return !TRANSFORMS.get().isEmpty();
   }

   private UiScale() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   private record Transform(float scale, float pivotX, float pivotY) {
   }
}
