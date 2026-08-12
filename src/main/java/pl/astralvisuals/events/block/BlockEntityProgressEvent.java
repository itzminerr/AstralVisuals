package pl.astralvisuals.events.block;

import net.minecraft.class_2586;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public record BlockEntityProgressEvent(class_2586 blockEntity, BlockEntityProgressEvent.Type type) implements Event {
   public static enum Type {
      ADD,
      REMOVE;
   }
}
