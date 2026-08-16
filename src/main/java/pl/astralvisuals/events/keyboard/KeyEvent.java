package pl.astralvisuals.events.keyboard;

import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;
import pl.astralvisuals.utils.client.managers.event.events.Event;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public record KeyEvent(class_437 screen, class_307 type, int key, int action) implements Event, QuickImports {
   public boolean matches(int key) {
      class_307 expectedType = key >= 0 && key < 8 ? class_307.field_1672 : class_307.field_1668;
      return this.key == key && this.type == expectedType;
   }

   public boolean isKeyDown(int key) {
      return this.isKeyDown(key, mc.field_1755 == null);
   }

   public boolean isKeyDown(int key, boolean screen) {
      return this.matches(key) && this.action == 1 && screen;
   }

   public boolean isKeyReleased(int key) {
      return this.isKeyReleased(key, mc.field_1755 == null);
   }

   public boolean isKeyReleased(int key, boolean screen) {
      return this.matches(key) && this.action == 0 && screen;
   }
}
