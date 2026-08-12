package pl.astralvisuals.events.item;

import net.minecraft.class_1268;
import net.minecraft.class_4587;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class HandAnimationEvent extends EventCancellable {
   private class_4587 matrices;
   private class_1268 hand;
   private float swingProgress;

   public HandAnimationEvent(class_4587 matrices, class_1268 hand, float swingProgress) {
      this.matrices = matrices;
      this.hand = hand;
      this.swingProgress = swingProgress;
   }

   public class_4587 getMatrices() {
      return this.matrices;
   }

   public class_1268 getHand() {
      return this.hand;
   }

   public float getSwingProgress() {
      return this.swingProgress;
   }

   public void setMatrices(class_4587 matrices) {
      this.matrices = matrices;
   }

   public void setHand(class_1268 hand) {
      this.hand = hand;
   }

   public void setSwingProgress(float swingProgress) {
      this.swingProgress = swingProgress;
   }
}
