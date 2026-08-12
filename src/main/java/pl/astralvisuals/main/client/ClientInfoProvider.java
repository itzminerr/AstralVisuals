package pl.astralvisuals.main.client;

import java.io.File;

public interface ClientInfoProvider {
   String clientName();

   String getFullInfo();

   File clientDir();

   File filesDir();

   File configsDir();
}
