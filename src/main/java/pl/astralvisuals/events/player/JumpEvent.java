package pl.astralvisuals.events.player;

import net.minecraft.class_1657;
import pl.astralvisuals.utils.client.managers.event.events.callables.EventCancellable;

public class JumpEvent extends EventCancellable {
   private class_1657 player;

   public class_1657 getPlayer() {
      return this.player;
   }

   public JumpEvent(class_1657 player) {
      this.player = player;
   }
}
