package pl.astralvisuals.events.player;

import net.minecraft.class_1282;
import net.minecraft.class_1297;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public class EntityDeathEvent implements Event {
   private final class_1297 entity;
   private final class_1282 source;

   public EntityDeathEvent(class_1297 entity, class_1282 source) {
      this.entity = entity;
      this.source = source;
   }

   public class_1297 getEntity() {
      return this.entity;
   }

   public class_1282 getSource() {
      return this.source;
   }
}
