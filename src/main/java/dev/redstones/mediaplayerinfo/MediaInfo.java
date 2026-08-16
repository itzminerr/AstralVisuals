package dev.redstones.mediaplayerinfo;

import java.util.Arrays;

/** Immutable snapshot returned by the native system media-session bridge. */
public final class MediaInfo {
   private final String title;
   private final String artist;
   private final byte[] artworkPng;
   private final long position;
   private final long duration;
   private final boolean playing;

   public MediaInfo(String title, String artist, byte[] artworkPng, long position, long duration, boolean playing) {
      this.title = title == null ? "" : title;
      this.artist = artist == null ? "" : artist;
      this.artworkPng = artworkPng == null ? new byte[0] : artworkPng;
      this.position = position;
      this.duration = duration;
      this.playing = playing;
   }

   public String getTitle() {
      return this.title;
   }

   public String getArtist() {
      return this.artist;
   }

   public byte[] getArtworkPng() {
      return this.artworkPng;
   }

   public long getPosition() {
      return this.position;
   }

   public long getDuration() {
      return this.duration;
   }

   public boolean getPlaying() {
      return this.playing;
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      }
      if (!(object instanceof MediaInfo other)) {
         return false;
      }
      return this.position == other.position
         && this.duration == other.duration
         && this.playing == other.playing
         && this.title.equals(other.title)
         && this.artist.equals(other.artist)
         && Arrays.equals(this.artworkPng, other.artworkPng);
   }

   @Override
   public int hashCode() {
      int result = this.title.hashCode();
      result = 31 * result + this.artist.hashCode();
      result = 31 * result + Arrays.hashCode(this.artworkPng);
      result = 31 * result + Long.hashCode(this.position);
      result = 31 * result + Long.hashCode(this.duration);
      return 31 * result + Boolean.hashCode(this.playing);
   }
}
