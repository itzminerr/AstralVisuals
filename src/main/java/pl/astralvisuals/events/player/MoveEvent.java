package pl.astralvisuals.events.player;

import net.minecraft.class_243;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public class MoveEvent implements Event {
   private class_243 movement;

   public class_243 getMovement() {
      return this.movement;
   }

   public void setMovement(class_243 movement) {
      this.movement = movement;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof MoveEvent other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         Object this$movement = this.getMovement();
         Object other$movement = other.getMovement();
         return this$movement == null ? other$movement == null : this$movement.equals(other$movement);
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof MoveEvent;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $movement = this.getMovement();
      return result * 59 + ($movement == null ? 43 : $movement.hashCode());
   }

   @Override
   public String toString() {
      return "MoveEvent(movement=" + this.getMovement() + ")";
   }

   public MoveEvent(class_243 movement) {
      this.movement = movement;
   }
}
