package dev.redstones.mediaplayerinfo;

import dev.redstones.mediaplayerinfo.impl.win.WindowsMediaPlayerInfo;
import java.util.List;

public final class MediaPlayerInfo {
   private MediaPlayerInfo() {
   }

   public static List<IMediaSession> getMediaSessions() {
      if (!WindowsMediaPlayerInfo.isAvailable()) {
         return List.of();
      }
      try {
         List<IMediaSession> sessions = WindowsMediaPlayerInfo.INSTANCE.getMediaSessions();
         return sessions == null ? List.of() : sessions;
      } catch (Throwable ignored) {
         return List.of();
      }
   }
}
