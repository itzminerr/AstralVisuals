package pl.astralvisuals.utils.client.managers.event.events.callables;

import pl.astralvisuals.utils.client.managers.event.events.Cancellable;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public abstract class EventCancellable implements Event, Cancellable {
   private boolean cancelled;

   protected EventCancellable() {
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void cancel() {
      this.cancelled = true;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
