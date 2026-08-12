package pl.astralvisuals.features.impl.movement;

import pl.astralvisuals.events.player.DeathScreenEvent;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleCategory;
import pl.astralvisuals.utils.client.Instance;
import pl.astralvisuals.utils.client.managers.event.EventHandler;

// Авто-возрождение: DeathScreenEvent шлётся каждый кадр экрана смерти (DeathScreenMixin),
// сразу вызываем requestRespawn() (method_7331) и закрываем экран.
public class AutoRespawn extends Module {
   public static AutoRespawn getInstance() {
      return Instance.get(AutoRespawn.class);
   }

   public AutoRespawn() {
      super("AutoRespawn", "Auto Respawn", ModuleCategory.PLAYER);
   }

   @EventHandler
   public void onDeathScreen(DeathScreenEvent e) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_7331();
         mc.method_1507(null);
      }
   }
}
