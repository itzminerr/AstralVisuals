package pl.astralvisuals.utils.client.target;

import net.minecraft.class_1309;
import net.minecraft.class_3966;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public final class TargetTracker implements QuickImports {
   private static class_1309 target;
   private static long targetTime;

   public static void setTarget(class_1309 livingEntity) {
      if (livingEntity != null && livingEntity != mc.field_1724) {
         target = livingEntity;
         targetTime = System.currentTimeMillis();
      }
   }

   public static class_1309 getTarget() {
      class_1309 crosshairTarget = getCrosshairTarget();
      return isValid(target) && System.currentTimeMillis() - targetTime <= 2500L ? target : crosshairTarget;
   }

   public static void tick() {
      if (!isValid(target) || System.currentTimeMillis() - targetTime > 2500L) {
         target = null;
      }
   }

   private static class_1309 getCrosshairTarget() {
      if (PlayerInteractionHelper.nullCheck()) {
         return null;
      } else {
         return mc.field_1765 instanceof class_3966 hitResult
               && hitResult.method_17782() instanceof class_1309 livingEntity
               && livingEntity != mc.field_1724
               && livingEntity.method_5805()
               && !livingEntity.method_31481()
            ? livingEntity
            : null;
      }
   }

   private static boolean isValid(class_1309 livingEntity) {
      return livingEntity != null
         && mc.field_1687 != null
         && livingEntity.method_37908() == mc.field_1687
         && livingEntity.method_5805()
         && !livingEntity.method_31481();
   }

   private TargetTracker() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
