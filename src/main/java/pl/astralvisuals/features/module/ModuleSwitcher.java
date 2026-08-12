package pl.astralvisuals.features.module;

import java.util.List;
import net.minecraft.class_124;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.features.module.exception.ModuleException;
import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class ModuleSwitcher implements QuickLogger, QuickImports {
   private final List<Module> modules;

   public ModuleSwitcher(List<Module> modules, EventManager eventManager) {
      this.modules = modules;
      eventManager.register(this);
   }

   @EventHandler
   public void onKey(KeyEvent event) {
      for (Module module : this.modules) {
         if (event.key() == module.getKey() && mc.field_1755 == null) {
            try {
               this.handleModuleState(module, event.action());
            } catch (Exception var5) {
               this.handleException(module.getName(), var5);
            }
         }
      }
   }

   private void handleModuleState(Module module, int action) {
      if (module.getType() == 1 && action == 1) {
         module.switchState();
      }
   }

   private void handleException(String moduleName, Exception e) {
      if (e instanceof ModuleException) {
         this.logDirect("[" + moduleName + "] " + class_124.field_1061 + e.getMessage());
      } else {
         Logger.error("Error in module " + moduleName + ": " + e.getMessage(), e);
      }
   }
}
