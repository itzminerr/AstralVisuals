package pl.astralvisuals.utils.client.discord.utils;

import java.io.Serializable;

public class RPCButton implements Serializable {
   private final String label;
   private final String url;

   public static RPCButton create(String label, String url) {
      return new RPCButton(label.substring(0, Math.min(label.length(), 31)), url);
   }

   private RPCButton(String label, String url) {
      this.label = label;
      this.url = url;
   }

   public String getLabel() {
      return this.label;
   }

   public String getUrl() {
      return this.url;
   }
}
