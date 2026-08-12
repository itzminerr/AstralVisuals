package pl.astralvisuals.utils.client.discord.utils;

import com.sun.jna.Structure;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordRichPresence extends Structure {
   public String state;
   public String details;
   public long startTimestamp;
   public long endTimestamp;
   public String largeImageKey;
   public String largeImageText;
   public String smallImageKey;
   public String smallImageText;
   public String partyId;
   public int partySize;
   public int partyMax;
   public String partyPrivacy;
   public String matchSecret;
   public String joinSecret;
   public String spectateSecret;
   public String button_label_1;
   public String button_url_1;
   public String button_label_2;
   public String button_url_2;
   public int instance;

   public DiscordRichPresence() {
      this.setStringEncoding("UTF-8");
   }

   protected List<String> getFieldOrder() {
      return Arrays.asList(
         "state",
         "details",
         "startTimestamp",
         "endTimestamp",
         "largeImageKey",
         "largeImageText",
         "smallImageKey",
         "smallImageText",
         "partyId",
         "partySize",
         "partyMax",
         "partyPrivacy",
         "matchSecret",
         "joinSecret",
         "spectateSecret",
         "button_label_1",
         "button_url_1",
         "button_label_2",
         "button_url_2",
         "instance"
      );
   }

   public static class Builder {
      private final DiscordRichPresence richPresence = new DiscordRichPresence();

      public DiscordRichPresence.Builder setDetails(String value) {
         if (value != null && !value.isEmpty()) {
            this.richPresence.details = value.substring(0, Math.min(value.length(), 128));
         }

         return this;
      }

      public DiscordRichPresence.Builder setState(String value) {
         if (value != null && !value.isEmpty()) {
            this.richPresence.state = value.substring(0, Math.min(value.length(), 128));
         }

         return this;
      }

      public DiscordRichPresence.Builder setStartTimestamp(long value) {
         this.richPresence.startTimestamp = value;
         return this;
      }

      public DiscordRichPresence.Builder setStartTimestamp(OffsetDateTime value) {
         this.richPresence.startTimestamp = value.toEpochSecond();
         return this;
      }

      public DiscordRichPresence.Builder setEndTimestamp(long value) {
         this.richPresence.endTimestamp = value;
         return this;
      }

      public DiscordRichPresence.Builder setEndTimestamp(OffsetDateTime value) {
         this.richPresence.endTimestamp = value.toEpochSecond();
         return this;
      }

      public DiscordRichPresence.Builder setLargeImage(String key) {
         return this.setLargeImage(key, "");
      }

      public DiscordRichPresence.Builder setLargeImage(String key, String text) {
         this.richPresence.largeImageKey = key;
         this.richPresence.largeImageText = text;
         return this;
      }

      public DiscordRichPresence.Builder setSmallImage(String key) {
         return this.setSmallImage(key, "");
      }

      public DiscordRichPresence.Builder setSmallImage(String key, String text) {
         this.richPresence.smallImageKey = key;
         this.richPresence.smallImageText = text;
         return this;
      }

      public DiscordRichPresence.Builder setButtons(RPCButton button) {
         return this.setButtons(Collections.singletonList(button));
      }

      public DiscordRichPresence.Builder setButtons(RPCButton first, RPCButton second) {
         return this.setButtons(Arrays.asList(first, second));
      }

      public DiscordRichPresence.Builder setButtons(List<RPCButton> buttons) {
         if (buttons != null && !buttons.isEmpty()) {
            this.richPresence.button_label_1 = buttons.get(0).getLabel();
            this.richPresence.button_url_1 = buttons.get(0).getUrl();
            if (buttons.size() > 1) {
               this.richPresence.button_label_2 = buttons.get(1).getLabel();
               this.richPresence.button_url_2 = buttons.get(1).getUrl();
            }

            return this;
         } else {
            return this;
         }
      }

      public DiscordRichPresence build() {
         return this.richPresence;
      }
   }
}
