package pl.astralvisuals.utils.update;

import net.fabricmc.loader.api.FabricLoader;

public final class ClientVersion {
   private static final String MOD_ID = "astralvisuals";
   private static final String LOADED_VERSION_PROPERTY = "astralvisuals.client.version";

   private ClientVersion() {
   }

   public static String get() {
      String loadedVersion = System.getProperty(LOADED_VERSION_PROPERTY);
      if (loadedVersion != null && !loadedVersion.isBlank()) {
         return loadedVersion;
      }
      return FabricLoader.getInstance()
         .getModContainer(MOD_ID)
         .map(container -> container.getMetadata().getVersion().getFriendlyString())
         .orElse("unknown");
   }

   public static String displayName() {
      return "AstralVisuals " + get();
   }
}
