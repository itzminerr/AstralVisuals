package pl.astralvisuals.main.client;

import net.minecraft.class_2596;
import net.minecraft.class_310;

public interface Api {
   class_310 mc = class_310.method_1551();

   static void sendPacket(class_2596<?> packet) {
      if (mc.method_1562() != null) {
         mc.method_1562().method_52787(packet);
      }
   }
}
