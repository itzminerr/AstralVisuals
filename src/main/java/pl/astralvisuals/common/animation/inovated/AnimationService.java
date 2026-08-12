package pl.astralvisuals.common.animation.inovated;

import net.minecraft.class_310;
import net.minecraft.class_3532;

public final class AnimationService {
   public static double delta;

   public static float animation(float animation, float target, float speedTarget) {
      float dif = (target - animation) / Math.max((float)class_310.method_1551().method_47599(), 5.0F) * 15.0F;
      if (dif > 0.0F) {
         dif = Math.max(speedTarget, dif);
         dif = Math.min(target - animation, dif);
      } else if (dif < 0.0F) {
         dif = Math.min(-speedTarget, dif);
         dif = Math.max(target - animation, dif);
      }

      return animation + dif;
   }

   public static float animationSpeed(float animation, float target, float speedTarget) {
      float dif = (target - animation) / Math.max((float)class_310.method_1551().method_47599(), 5.0F) * 15.0F;
      if (dif > 0.0F) {
         dif = Math.min(target - animation, speedTarget);
      } else if (dif < 0.0F) {
         dif = -speedTarget;
         dif = Math.max(target - animation, dif);
      }

      return animation + dif;
   }

   public static float getAnimationState(float animation, float finalState, float speed) {
      float add = (float)(delta * (speed / 1000.0F));
      if (animation < finalState) {
         if (animation + add < finalState) {
            animation += add;
         } else {
            animation = finalState;
         }
      } else if (animation - add > finalState) {
         animation -= add;
      } else {
         animation = finalState;
      }

      return animation;
   }

   public static double interpolateAnimation(double start, double end, double step) {
      return start + (end - start) * step;
   }

   public static float move(float from, float to, float minstep, float maxstep, float factor) {
      float f = (to - from) * class_3532.method_15363(factor, 0.0F, 1.0F);
      if (f < 0.0F) {
         f = class_3532.method_15363(f, -maxstep, -minstep);
      } else {
         f = class_3532.method_15363(f, minstep, maxstep);
      }

      return Math.abs(f) > Math.abs(to - from) ? to : from + f;
   }

   private AnimationService() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
