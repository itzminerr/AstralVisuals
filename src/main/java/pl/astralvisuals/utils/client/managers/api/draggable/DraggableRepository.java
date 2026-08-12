package pl.astralvisuals.utils.client.managers.api.draggable;

import java.util.ArrayList;
import java.util.List;
import pl.astralvisuals.display.hud.Coordinates;
import pl.astralvisuals.display.hud.HotKeys;
import pl.astralvisuals.display.hud.Inventory;
import pl.astralvisuals.display.hud.Notifications;
import pl.astralvisuals.display.hud.Potions;
import pl.astralvisuals.display.hud.TargetHud;
import pl.astralvisuals.display.hud.Watermark;
import pl.astralvisuals.display.hud.Waypoints;

public class DraggableRepository {
   private final List<AbstractDraggable> draggable = new ArrayList<>();

   public void setup() {
      this.register(new Potions(), new HotKeys(), new Watermark(), new Inventory(), new Notifications(), new TargetHud(), new Coordinates(), new Waypoints());
   }

   public void register(AbstractDraggable... module) {
      this.draggable.addAll(List.of(module));
   }

   public List<AbstractDraggable> draggable() {
      return this.draggable;
   }

   public <T extends AbstractDraggable> T get(String name) {
      return this.draggable.stream().filter(module -> module.getName().equalsIgnoreCase(name)).map(module -> (T)module).findFirst().orElse(null);
   }

   public <T extends AbstractDraggable> T get(Class<T> clazz) {
      return this.draggable.stream().filter(module -> clazz.isAssignableFrom(module.getClass())).map(clazz::cast).findFirst().orElse(null);
   }
}
