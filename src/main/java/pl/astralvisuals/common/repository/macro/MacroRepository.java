package pl.astralvisuals.common.repository.macro;

import java.util.ArrayList;
import java.util.List;
import pl.astralvisuals.events.keyboard.KeyEvent;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class MacroRepository implements QuickImports, QuickLogger {
   public List<Macro> macroList = new ArrayList<>();

   public MacroRepository(EventManager eventManager) {
      eventManager.register(this);
   }

   public void addMacro(String name, String message, int key) {
      this.macroList.add(new Macro(name, message, key));
   }

   public boolean hasMacro(String text) {
      return this.macroList.stream().anyMatch(macro -> macro.name().equalsIgnoreCase(text));
   }

   public void deleteMacro(String text) {
      this.macroList.removeIf(macro -> macro.name().equalsIgnoreCase(text));
   }

   public void clearList() {
      this.macroList.clear();
   }

   @EventHandler
   public void onKey(KeyEvent e) {
      if (mc.field_1724 != null && e.action() == 0 && mc.field_1755 == null) {
         this.macroList.stream().filter(macro -> e.matches(macro.key())).findFirst().ifPresent(macro -> mc.field_1724.field_3944.method_45729(macro.message()));
      }
   }
}
