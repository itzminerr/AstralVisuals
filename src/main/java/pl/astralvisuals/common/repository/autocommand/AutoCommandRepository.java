package pl.astralvisuals.common.repository.autocommand;

import java.util.ArrayList;
import java.util.List;
import pl.astralvisuals.events.player.TickEvent;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class AutoCommandRepository implements QuickImports {
   public List<AutoCommandEntry> entries = new ArrayList<>();
   private boolean stopped = false;

   public AutoCommandRepository(EventManager eventManager) {
      eventManager.register(this);
   }

   public boolean hasEntry(String name) {
      return this.entries.stream().anyMatch(entry -> entry.name().equalsIgnoreCase(name));
   }

   public void addEntry(String name, String command, int delaySeconds) {
      this.entries.add(new AutoCommandEntry(name, command, delaySeconds * 1000L));
   }

   public boolean removeEntry(String name) {
      return this.entries.removeIf(entry -> entry.name().equalsIgnoreCase(name));
   }

   public void clearEntry() {
      this.entries.clear();
   }

   public boolean isStopped() {
      return this.stopped;
   }

   public void stop() {
      this.stopped = true;
   }

   public void start() {
      this.stopped = false;
      long now = System.currentTimeMillis();
      this.entries.forEach(entry -> entry.setNextRun(now + entry.delayMs()));
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (this.stopped || this.entries.isEmpty() || mc.field_1724 == null || mc.field_1724.field_3944 == null) {
         return;
      }

      long now = System.currentTimeMillis();
      for (AutoCommandEntry entry : this.entries) {
         if (entry.isActive() && now >= entry.nextRun()) {
            mc.field_1724.field_3944.method_45729(entry.command());
            entry.setNextRun(now + entry.delayMs());
         }
      }
   }
}
