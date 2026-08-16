package dev.redstones.mediaplayerinfo.impl.win;

import dev.redstones.mediaplayerinfo.IMediaSession;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class WindowsMediaPlayerInfo {
   public static final WindowsMediaPlayerInfo INSTANCE = new WindowsMediaPlayerInfo();
   private static final boolean AVAILABLE = loadNativeLibrary();

   private WindowsMediaPlayerInfo() {
   }

   public static boolean isAvailable() {
      return AVAILABLE;
   }

   public native List<IMediaSession> getMediaSessions();

   private static boolean loadNativeLibrary() {
      if (!System.getProperty("os.name", "").toLowerCase().startsWith("windows")) {
         return false;
      }
      try (InputStream input = WindowsMediaPlayerInfo.class.getResourceAsStream("/mediaplayerinfo/natives/win/MediaPlayerInfo.dll")) {
         if (input == null) {
            return false;
         }
         Path directory = Files.createTempDirectory("astralvisuals-media-");
         Path library = directory.resolve("MediaPlayerInfo.dll");
         Files.copy(input, library);
         directory.toFile().deleteOnExit();
         library.toFile().deleteOnExit();
         System.load(library.toAbsolutePath().toString());
         return true;
      } catch (Throwable ignored) {
         return false;
      }
   }
}
