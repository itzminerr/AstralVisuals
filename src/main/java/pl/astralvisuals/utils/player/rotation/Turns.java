package pl.astralvisuals.utils.player.rotation;

import net.minecraft.class_243;
import net.minecraft.class_3532;
import pl.astralvisuals.utils.math.calc.Calculate;

public class Turns {
   public static final Turns DEFAULT = new Turns(0.0F, 0.0F);
   private float yaw;
   private float pitch;

   public static Turns fromTargetHead(class_243 playerPos, class_243 targetPos, double targetHeight) {
      double headY = targetPos.field_1351 + targetHeight * 0.9;
      double deltaX = targetPos.field_1352 - playerPos.field_1352;
      double deltaY = headY - (playerPos.field_1351 + 1.5);
      double deltaZ = targetPos.field_1350 - playerPos.field_1350;
      float yaw = class_3532.method_15393((float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0F);
      double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      float pitch = class_3532.method_15363((float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance)), -90.0F, 90.0F);
      return new Turns(yaw, pitch);
   }

   public Turns adjustSensitivity() {
      double gcd = Calculate.computeGcd();
      float adjustedYaw = Math.round(this.yaw / (float)gcd) * (float)gcd;
      float adjustedPitch = Math.round(this.pitch / (float)gcd) * (float)gcd;
      return new Turns(class_3532.method_15393(adjustedYaw), class_3532.method_15363(adjustedPitch, -90.0F, 90.0F));
   }

   public Turns random(float value) {
      return new Turns(this.yaw + Calculate.getRandom(-value, value), this.pitch + Calculate.getRandom(-value, value));
   }

   public class_243 toVector() {
      float f = this.pitch * (float) (Math.PI / 180.0);
      float g = -this.yaw * (float) (Math.PI / 180.0);
      float h = class_3532.method_15362(g);
      float i = class_3532.method_15374(g);
      float j = class_3532.method_15362(f);
      float k = class_3532.method_15374(f);
      return new class_243(i * j, -k, h * j);
   }

   public Turns addYaw(float yaw) {
      return new Turns(this.yaw + yaw, this.pitch);
   }

   public Turns addPitch(float pitch) {
      this.pitch = class_3532.method_15363(this.pitch + pitch, -90.0F, 90.0F);
      return this;
   }

   public Turns of(Turns angle) {
      return new Turns(angle.getYaw(), angle.getPitch());
   }

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Override
   public String toString() {
      return "Turns(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

   public Turns(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public static class VecRotation {
      private final Turns angle;
      private final class_243 vec;

      @Override
      public String toString() {
         return "Turns.VecRotation(angle=" + this.getAngle() + ", vec=" + this.getVec() + ")";
      }

      public Turns getAngle() {
         return this.angle;
      }

      public class_243 getVec() {
         return this.vec;
      }

      public VecRotation(Turns angle, class_243 vec) {
         this.angle = angle;
         this.vec = vec;
      }
   }
}
