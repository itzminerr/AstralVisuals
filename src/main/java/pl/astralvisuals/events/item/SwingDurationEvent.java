package pl.astralvisuals.events.item;

import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class SwingDurationEvent extends EventCancellable {
   private float animation;

   public float getAnimation() {
      return this.animation;
   }

   public void setAnimation(float animation) {
      this.animation = animation;
   }
}
