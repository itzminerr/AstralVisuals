package pl.astralvisuals.utils.client;

import java.lang.reflect.Field;
import net.minecraft.class_591;

public class ReflectionUtils {
   private static Field modelField;

   static {
      try {
         Class<?> rendererClass = Class.forName("net.minecraft.class_1007");
         try {
            modelField = rendererClass.getDeclaredField("field_1723");
            modelField.setAccessible(true);
         } catch (NoSuchFieldException e1) {
            for (Field f : rendererClass.getDeclaredFields()) {
               if (f.getType().getName().equals("net.minecraft.class_591")) {
                  f.setAccessible(true);
                  modelField = f;
                  break;
               }
            }
         }
      } catch (Exception ignored) {}
   }

   public static class_591 getPlayerModel(Object playerRenderer) {
      if (modelField == null) return null;
      try {
         return (class_591) modelField.get(playerRenderer);
      } catch (Exception e) {
         return null;
      }
   }
}
