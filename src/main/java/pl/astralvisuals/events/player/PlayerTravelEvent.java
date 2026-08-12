package pl.astralvisuals.events.player;

import net.minecraft.class_243;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class PlayerTravelEvent extends EventCancellable {
   private class_243 motion;
   private final boolean pre;

   public PlayerTravelEvent(class_243 motion, boolean pre) {
      this.motion = motion;
      this.pre = pre;
   }

   public class_243 getMotion() {
      return this.motion;
   }

   public void setMotion(class_243 motion) {
      this.motion = motion;
   }

   public boolean isPre() {
      return this.pre;
   }
}
