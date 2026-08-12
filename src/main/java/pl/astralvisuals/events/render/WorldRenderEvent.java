package pl.astralvisuals.events.render;

import net.minecraft.class_4587;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public class WorldRenderEvent implements Event {
   private class_4587 stack;
   private float partialTicks;

   public WorldRenderEvent(class_4587 stack, float partialTicks) {
      this.stack = stack;
      this.partialTicks = partialTicks;
   }

   public class_4587 getStack() {
      return this.stack;
   }

   public float getPartialTicks() {
      return this.partialTicks;
   }
}
