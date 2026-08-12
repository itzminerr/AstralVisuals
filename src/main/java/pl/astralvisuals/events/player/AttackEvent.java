package pl.astralvisuals.events.player;

import net.minecraft.class_1297;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class AttackEvent extends EventCancellable {
   private class_1297 entity;

   public AttackEvent(class_1297 entity) {
      this.entity = entity;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public void setEntity(class_1297 entity) {
      this.entity = entity;
   }
}
