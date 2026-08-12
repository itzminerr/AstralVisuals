package pl.astralvisuals.utils.interactions.simulate;

import java.util.Objects;
import net.minecraft.class_10185;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public final class Simulations implements QuickImports {
   public static boolean hasPlayerMovement() {
      return mc.field_1724.field_3913.field_3905 != 0.0F || mc.field_1724.field_3913.field_3907 != 0.0F;
   }

   public static double[] calculateDirection(double distance) {
      return calculateDirection(mc.field_1724.field_3913.field_3905, mc.field_1724.field_3913.field_3907, distance);
   }

   public static boolean moveKeyPressed(int keyNumber) {
      boolean w = mc.field_1690.field_1894.method_1434();
      boolean a = mc.field_1690.field_1913.method_1434();
      boolean s = mc.field_1690.field_1881.method_1434();
      boolean d = mc.field_1690.field_1849.method_1434();
      return keyNumber == 0 ? w : (keyNumber == 1 ? a : (keyNumber == 2 ? s : keyNumber == 3 && d));
   }

   public static boolean w() {
      return moveKeyPressed(0);
   }

   public static boolean a() {
      return moveKeyPressed(1);
   }

   public static boolean s() {
      return moveKeyPressed(2);
   }

   public static boolean d() {
      return moveKeyPressed(3);
   }

   public static float moveYaw(float entityYaw) {
      return entityYaw
         + (
            !a() || !d() || w() && s() || !w() && !s()
               ? (
                  !w() || !s() || a() && d() || !a() && !d()
                     ? (
                        (!a() || !d() || w() && s()) && (!w() || !s() || a() && d())
                           ? (
                              !a() && !d() && !s()
                                 ? 0
                                 : (w() && !s() ? 45 : (!s() || w() ? (!w() && !s() || w() && s() ? 90 : 0) : (!a() && !d() ? 180 : 135))) * (a() ? -1 : 1)
                           )
                           : 0
                     )
                     : (a() ? -90 : (d() ? 90 : 0))
               )
               : (w() ? 0 : (s() ? 180 : 0))
         );
   }

   public static double[] forward(double distance) {
      float forward = mc.field_1724.field_3913.field_3905;
      float sideways = mc.field_1724.field_3913.field_3907;
      float yaw = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sin = Math.sin(Math.toRadians(yaw + 90.0F));
      double cos = Math.cos(Math.toRadians(yaw + 90.0F));
      return new double[]{forward * distance * cos + sideways * distance * sin, forward * distance * sin - sideways * distance * cos};
   }

   public static float calculateBodyYaw(float yaw, float prevBodyYaw, double prevX, double prevZ, double currentX, double currentZ, float handSwingProgress) {
      double motionX = currentX - prevX;
      double motionZ = currentZ - prevZ;
      float motionSquared = (float)(motionX * motionX + motionZ * motionZ);
      float bodyYaw = prevBodyYaw;
      if (motionSquared > 0.0025000002F) {
         float movementYaw = (float)class_3532.method_15349(motionZ, motionX) * (180.0F / (float)Math.PI) - 90.0F;
         float yawDiff = class_3532.method_15379(class_3532.method_15393(yaw) - movementYaw);
         bodyYaw = 95.0F < yawDiff && yawDiff < 265.0F ? movementYaw - 180.0F : movementYaw;
      }

      if (mc.field_1724 != null && mc.field_1724.field_6251 - 0.2F > 0.0F) {
         bodyYaw = yaw;
      }

      float deltaYaw = class_3532.method_15393(bodyYaw - prevBodyYaw);
      bodyYaw = prevBodyYaw + deltaYaw * 0.3F;
      float yawOffsetDiff = class_3532.method_15393(yaw - bodyYaw);
      float maxHeadRotation = 52.0F;
      if (Math.abs(yawOffsetDiff) > maxHeadRotation) {
         bodyYaw += yawOffsetDiff - class_3532.method_17822(yawOffsetDiff) * maxHeadRotation;
      }

      return bodyYaw;
   }

   public static double[] calculateDirection(float forward, float sideways, double distance) {
      float yaw = mc.field_1724.method_36454();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians(yaw + 90.0F));
      double cosYaw = Math.cos(Math.toRadians(yaw + 90.0F));
      return new double[]{forward * distance * cosYaw + sideways * distance * sinYaw, forward * distance * sinYaw - sideways * distance * cosYaw};
   }

   public static double getSpeedSqrt(class_1297 entity) {
      return Math.sqrt(entity.method_5707(new class_243(entity.field_6014, entity.field_6036, entity.field_5969)));
   }

   public static void setVelocity(double velocity) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], mc.field_1724.method_18798().method_10214(), direction[1]);
   }

   public static void setVelocity(double velocity, double y) {
      double[] direction = calculateDirection(velocity);
      Objects.requireNonNull(mc.field_1724).method_18800(direction[0], y, direction[1]);
   }

   public static double getDegreesRelativeToView(class_243 positionRelativeToPlayer, float yaw) {
      float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.field_1352, positionRelativeToPlayer.field_1350);
      double currentYaw = Math.toRadians(class_3532.method_15393(yaw));
      return Math.toDegrees(class_3532.method_15338(optimalYaw - currentYaw));
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double degrees, float deadAngle) {
      boolean forwards = input.comp_3159();
      boolean backwards = input.comp_3160();
      boolean left = input.comp_3161();
      boolean right = input.comp_3162();
      if (degrees >= -90.0F + deadAngle && degrees <= 90.0F - deadAngle) {
         forwards = true;
      } else if (degrees < -90.0F - deadAngle || degrees > 90.0F + deadAngle) {
         backwards = true;
      }

      if (degrees >= 0.0F + deadAngle && degrees <= 180.0F - deadAngle) {
         right = true;
      } else if (degrees >= -180.0F + deadAngle && degrees <= 0.0F - deadAngle) {
         left = true;
      }

      return new class_10185(forwards, backwards, left, right, input.comp_3163(), input.comp_3164(), input.comp_3165());
   }

   public static class_10185 getDirectionalInputForDegrees(class_10185 input, double degrees) {
      return getDirectionalInputForDegrees(input, degrees, 20.0F);
   }

   private Simulations() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
