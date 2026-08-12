package pl.astralvisuals.common.repository.way;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2338;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.client.managers.file.impl.WayFile;
import pl.astralvisuals.utils.display.interfaces.QuickImports;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class WayRepository implements QuickImports, QuickLogger {
   private static final String SINGLEPLAYER_SERVER = "singleplayer";
   public List<Way> wayList = new ArrayList<>();

   public WayRepository(EventManager eventManager) {
      eventManager.register(this);
   }

   public boolean isEmpty() {
      return this.wayList.isEmpty();
   }

   public void addWay(String name, class_2338 pos, String server) {
      this.wayList.add(new Way(name, pos, server));
      this.saveWays();
   }

   public boolean hasWay(String text) {
      return this.wayList.stream().anyMatch(s -> s.name().equalsIgnoreCase(text));
   }

   public void deleteWay(String name) {
      this.wayList.removeIf(macro -> macro.name().equalsIgnoreCase(name));
      this.saveWays();
   }

   public void clearList() {
      if (!this.isEmpty()) {
         this.wayList.clear();
      }

      this.saveWays();
   }

   public String getCurrentServerKey() {
      return mc.method_1562() != null && mc.method_1562().method_45734() != null ? mc.method_1562().method_45734().field_3761 : "singleplayer";
   }

   private void saveWays() {
      Force force = Force.getInstance();
      if (force != null && force.getFileController() != null) {
         force.getFileController().saveFile(WayFile.class);
      }
   }
}
