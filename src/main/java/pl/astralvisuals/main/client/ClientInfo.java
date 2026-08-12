package pl.astralvisuals.main.client;

import java.io.File;

public record ClientInfo(String clientName, File clientDir, File filesDir) implements ClientInfoProvider {
   @Override
   public String getFullInfo() {
      return "";
   }

   @Override
   public File configsDir() {
      return this.clientDir;
   }
}
