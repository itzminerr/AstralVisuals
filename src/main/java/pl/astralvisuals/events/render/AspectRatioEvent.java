package pl.astralvisuals.events.render;

import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class AspectRatioEvent extends EventCancellable {
   private float ratio;

   public float getRatio() {
      return this.ratio;
   }

   public void setRatio(float ratio) {
      this.ratio = ratio;
   }
}
