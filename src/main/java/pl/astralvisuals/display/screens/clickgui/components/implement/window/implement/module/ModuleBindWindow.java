package pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.module;

import pl.astralvisuals.display.screens.clickgui.components.implement.window.implement.AbstractBindWindow;
import pl.astralvisuals.features.module.Module;

public class ModuleBindWindow extends AbstractBindWindow {
   private final Module module;

   @Override
   protected int getKey() {
      return this.module.getKey();
   }

   @Override
   protected void setKey(int key) {
      this.module.setKey(key);
   }

   @Override
   protected int getType() {
      return this.module.getType();
   }

   @Override
   protected void setType(int type) {
      this.module.setType(type);
   }

   public ModuleBindWindow(Module module) {
      this.module = module;
   }
}
