package pl.astralvisuals.utils.client.packet.network;

import net.minecraft.class_2761;
import net.minecraft.class_3532;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public final class Network implements QuickImports {
   public static float TPS = 20.0F;
   public static long timestamp;

   public static void tick() {
   }

   public static void packet(PacketEvent e) {
      switch (e.getPacket()) {
         case class_2761 ignored:
            long nanoTime = System.nanoTime();
            float maxTPS = 20.0F;
            float rawTPS = maxTPS * (1.0E9F / (float)(nanoTime - timestamp));
            TPS = class_3532.method_15363(rawTPS, 0.0F, maxTPS);
            timestamp = nanoTime;
         default:
      }
   }

   private Network() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
