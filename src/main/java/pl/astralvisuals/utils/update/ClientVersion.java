package pl.astralvisuals.utils.update;

import net.fabricmc.loader.api.FabricLoader;

public final class ClientVersion {
   private static final String MOD_ID = "astralvisuals";

   private ClientVersion() {
   }

   public static String get() {
      return FabricLoader.getInstance()
         .getModContainer(MOD_ID)
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown");
   }

   public static String displayName() {
      return "AstralVisuals " + get();
   }
}
