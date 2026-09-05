package pl.astralvisuals.common.repository.autocommand;

public class AutoCommandEntry {
   private final String name;
   private final String command;
   private final long delayMs;
   private long nextRun;
   private boolean active = true;

   public AutoCommandEntry(String name, String command, long delayMs) {
      this.name = name;
      this.command = command;
      this.delayMs = delayMs;
      this.nextRun = System.currentTimeMillis() + delayMs;
   }

   public String name() {
      return this.name;
   }

   public String command() {
      return this.command;
   }

   public long delayMs() {
      return this.delayMs;
   }

   public long nextRun() {
      return this.nextRun;
   }

   public void setNextRun(long nextRun) {
      this.nextRun = nextRun;
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }
}
