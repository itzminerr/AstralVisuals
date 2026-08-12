package pl.astralvisuals.events.container;

import net.minecraft.class_1735;
import net.minecraft.class_332;
import pl.astralvisuals.utils.client.managers.event.events.Event;

public class HandledScreenEvent implements Event {
   private class_332 drawContext;
   private class_1735 slotHover;
   private int backgroundWidth;
   private int backgroundHeight;

   public class_332 getDrawContext() {
      return this.drawContext;
   }

   public class_1735 getSlotHover() {
      return this.slotHover;
   }

   public int getBackgroundWidth() {
      return this.backgroundWidth;
   }

   public int getBackgroundHeight() {
      return this.backgroundHeight;
   }

   public HandledScreenEvent(class_332 drawContext, class_1735 slotHover, int backgroundWidth, int backgroundHeight) {
      this.drawContext = drawContext;
      this.slotHover = slotHover;
      this.backgroundWidth = backgroundWidth;
      this.backgroundHeight = backgroundHeight;
   }
}
