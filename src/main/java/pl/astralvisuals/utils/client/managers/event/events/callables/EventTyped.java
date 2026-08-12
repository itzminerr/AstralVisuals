package pl.astralvisuals.utils.client.managers.event.events.callables;

import pl.astralvisuals.utils.client.managers.event.events.Event;
import pl.astralvisuals.utils.client.managers.event.events.Typed;

public abstract class EventTyped implements Event, Typed {
   private final byte type;

   protected EventTyped(byte eventType) {
      this.type = eventType;
   }

   @Override
   public byte getType() {
      return this.type;
   }
}
