package pl.astralvisuals.features.impl.movement;

import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.discord.DiscordManager;

public class DiscordPresence extends Module {
   public DiscordPresence() {
      super("DiscordRPC", "Discord RPC", ModuleCategory.PLAYER);
      this.state = true; // включён по умолчанию
   }

   public static DiscordPresence getInstance() {
      return Instance.get(DiscordPresence.class);
   }

   @Override
   public void setState(boolean state) {
      boolean changed = state != this.state;
      super.setState(state);
      if (!changed) {
         return;
      }

      DiscordManager manager = Force.getInstance().getDiscordManager();
      if (manager == null) {
         return;
      }

      if (state) {
         if (!manager.isRunning()) {
            manager.init();
         }
      } else {
         manager.stopRPC();
      }
   }
}
