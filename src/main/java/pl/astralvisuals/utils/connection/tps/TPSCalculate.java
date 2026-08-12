package pl.astralvisuals.utils.connection.tps;

import net.minecraft.class_2761;
import net.minecraft.class_3532;
import pl.astralvisuals.Force;
import pl.astralvisuals.events.packet.PacketEvent;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

public class TPSCalculate {
   private float TPS = 20.0F;
   private float adjustTicks = 0.0F;
   private long timestamp;

   public TPSCalculate() {
      Force.getInstance().getEventManager().register(this);
   }

   @EventHandler
   private void onPacket(PacketEvent e) {
      if (e.getPacket() instanceof class_2761) {
         this.updateTPS();
      }
   }

   private void updateTPS() {
      long delay = System.nanoTime() - this.timestamp;
      float maxTPS = 20.0F;
      float rawTPS = maxTPS * (1.0E9F / (float)delay);
      float boundedTPS = class_3532.method_15363(rawTPS, 0.0F, maxTPS);
      this.TPS = (float)this.round(boundedTPS);
      this.adjustTicks = boundedTPS - maxTPS;
      this.timestamp = System.nanoTime();
   }

   public double round(double input) {
      return Math.round(input * 100.0) / 100.0;
   }

   public float getTPS() {
      return this.TPS;
   }

   public float getAdjustTicks() {
      return this.adjustTicks;
   }

   public long getTimestamp() {
      return this.timestamp;
   }
}
