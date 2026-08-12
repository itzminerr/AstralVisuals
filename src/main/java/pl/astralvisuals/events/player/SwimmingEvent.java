package pl.astralvisuals.events.player;

import net.minecraft.class_243;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class SwimmingEvent extends EventCancellable {
   class_243 vector;

   public void setVector(class_243 vector) {
      this.vector = vector;
   }

   public class_243 getVector() {
      return this.vector;
   }

   public SwimmingEvent(class_243 vector) {
      this.vector = vector;
   }
}
