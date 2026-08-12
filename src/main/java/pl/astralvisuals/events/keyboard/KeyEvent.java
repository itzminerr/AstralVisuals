package pl.astralvisuals.events.keyboard;

import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;
import pl.astralvisuals.utils.client.managers.event.events.Event;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public record KeyEvent(class_437 screen, class_307 type, int key, int action) implements Event, QuickImports {
   public boolean isKeyDown(int key) {
      return this.isKeyDown(key, mc.field_1755 == null);
   }

   public boolean isKeyDown(int key, boolean screen) {
      return this.key == key && this.action == 1 && screen;
   }

   public boolean isKeyReleased(int key) {
      return this.isKeyReleased(key, mc.field_1755 == null);
   }

   public boolean isKeyReleased(int key, boolean screen) {
      return this.key == key && this.action == 0 && screen;
   }
}
