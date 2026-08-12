package pl.astralvisuals.utils.client.managers.event.events;

public interface Cancellable {
   boolean isCancelled();

   void cancel();
}
