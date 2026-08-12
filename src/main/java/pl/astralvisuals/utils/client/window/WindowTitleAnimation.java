package pl.astralvisuals.utils.client.window;

public class WindowTitleAnimation {
   private static final WindowTitleAnimation INSTANCE = new WindowTitleAnimation();
   private static final String TITLE = "Minecraft 1.21.4";

   private WindowTitleAnimation() {
   }

   public static WindowTitleAnimation getInstance() {
      return INSTANCE;
   }

   public void updateTitle() {
   }

   public String getCurrentTitle() {
      return "Minecraft 1.21.4";
   }
}
