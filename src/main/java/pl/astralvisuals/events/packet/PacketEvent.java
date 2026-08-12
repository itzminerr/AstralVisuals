package pl.astralvisuals.events.packet;

import net.minecraft.class_2596;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class PacketEvent extends EventCancellable {
   private class_2596<?> packet;
   private PacketEvent.Type type;

   public boolean isSend() {
      return this.type.equals(PacketEvent.Type.SEND);
   }

   public class_2596<?> getPacket() {
      return this.packet;
   }

   public PacketEvent.Type getType() {
      return this.type;
   }

   public void setPacket(class_2596<?> packet) {
      this.packet = packet;
   }

   public void setType(PacketEvent.Type type) {
      this.type = type;
   }

   public PacketEvent(class_2596<?> packet, PacketEvent.Type type) {
      this.packet = packet;
      this.type = type;
   }

   public static enum Type {
      SEND,
      RECEIVE;
   }
}
