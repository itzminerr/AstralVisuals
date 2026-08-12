package pl.astralvisuals.events.player;

import net.minecraft.class_1297;
import net.minecraft.class_238;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class BoundingBoxControlEvent extends EventCancellable {
   public class_238 box;
   public class_1297 entity;

   public class_238 getBox() {
      return this.box;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public void setBox(class_238 box) {
      this.box = box;
   }

   public void setEntity(class_1297 entity) {
      this.entity = entity;
   }

   public BoundingBoxControlEvent(class_238 box, class_1297 entity) {
      this.box = box;
      this.entity = entity;
   }
}
