package pl.astralvisuals.utils.math;

public class Counter {
   public long lastMS = System.currentTimeMillis();
   private long time;

   public Counter() {
      this.resetCounter();
      this.reset();
   }

   public static Counter create() {
      return new Counter();
   }

   public void resetCounter() {
      this.lastMS = System.currentTimeMillis();
   }

   public void reset() {
      this.time = System.nanoTime();
   }

   public boolean isReached(long time) {
      return System.currentTimeMillis() - this.lastMS > time;
   }

   public void setLastMS(long newValue) {
      this.lastMS = System.currentTimeMillis() + newValue;
   }

   public void setTime(long time) {
      this.lastMS = time;
   }

   public long getPassedTimeMs() {
      return this.getMs(System.nanoTime() - this.time);
   }

   public long getMs(long time) {
      return time / 1000000L;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.lastMS;
   }

   public boolean passedMs(long ms) {
      return this.getMs(System.nanoTime() - this.time) >= ms;
   }

   public boolean isRunning() {
      return System.currentTimeMillis() - this.lastMS <= 0L;
   }

   public boolean hasTimeElapsed() {
      return this.lastMS < System.currentTimeMillis();
   }

   public long getLastMS() {
      return this.lastMS;
   }
}
