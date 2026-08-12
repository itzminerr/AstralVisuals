package pl.astralvisuals.utils.client.managers.file;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pl.astralvisuals.utils.client.logs.Logger;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;
import pl.astralvisuals.utils.client.managers.file.impl.ModuleFile;

public class FileController {
   private final List<ClientFile> clientFiles;
   private final File directory;
   private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, runnable -> {
      Thread thread = new Thread(runnable, "AstralVisuals-AutoSave");
      thread.setDaemon(true);
      return thread;
   });

   public FileController(List<ClientFile> clientFiles, File directory) {
      this.clientFiles = clientFiles;
      this.directory = directory;
      this.startAutoSave();
   }

   public void startAutoSave() {
      Logger.info("Auto-save system started!");
      this.scheduler.scheduleAtFixedRate(() -> {
         try {
            Logger.info("Saving with auto-save.");
            this.saveFiles();
         } catch (FileSaveException var2) {
            Logger.error("Failed to auto-save files: " + var2.getMessage());
         }
      }, 1L, 1L, TimeUnit.MINUTES);
   }

   public void stopAutoSave() {
      Logger.info("Auto-save shutdown!");
      this.scheduler.shutdown();

      try {
         if (!this.scheduler.awaitTermination(1L, TimeUnit.MINUTES)) {
            this.scheduler.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.scheduler.shutdownNow();
      }
   }

   public void saveFiles() throws FileSaveException {
      if (this.clientFiles.isEmpty()) {
         Logger.warn("No files to save from directory: " + this.directory.getPath());
      } else {
         for (ClientFile clientFile : this.clientFiles) {
            try {
               clientFile.saveToFile(this.directory);
               Logger.info("Successfully saved file: " + clientFile.getName() + " to " + this.directory.getPath());
            } catch (FileSaveException var4) {
               throw new FileSaveException("Failed to save file: " + clientFile.getName(), var4);
            }
         }
      }
   }

   public void loadFiles() throws FileLoadException {
      if (this.clientFiles.isEmpty()) {
         Logger.warn("No files to load from directory: " + this.directory.getPath());
      } else {
         for (ClientFile clientFile : this.clientFiles) {
            try {
               clientFile.loadFromFile(this.directory);
               Logger.info("Successfully loaded file: " + clientFile.getName() + " from " + this.directory.getPath());
            } catch (FileLoadException var4) {
               throw new FileLoadException("Failed to load file: " + clientFile.getName(), var4);
            }
         }
      }
   }

   public void saveFile(String fileName) throws FileSaveException {
      for (ClientFile clientFile : this.clientFiles) {
         if (clientFile instanceof ModuleFile) {
            try {
               clientFile.saveToFile(this.directory, fileName);
               Logger.info("Successfully saved file: " + fileName + " to " + this.directory.getPath());
            } catch (FileSaveException var5) {
               throw new FileSaveException("Failed to save file: " + fileName, var5);
            }
         }
      }
   }

   public void loadFile(String fileName) throws FileLoadException {
      for (ClientFile clientFile : this.clientFiles) {
         if (clientFile instanceof ModuleFile) {
            try {
               clientFile.loadFromFile(this.directory, fileName);
               Logger.info("Successfully loaded file: " + fileName + " from " + this.directory.getPath());
            } catch (FileLoadException var5) {
               throw new FileLoadException("Failed to load file: " + fileName, var5);
            }
         }
      }
   }

   public void saveFile(Class<? extends ClientFile> fileClass) {
      this.clientFiles.stream().filter(fileClass::isInstance).findFirst().ifPresent(file -> {
         try {
            file.saveToFile(this.directory);
            Logger.info("Successfully saved file on-demand: " + file.getName());
         } catch (FileSaveException var3) {
            Logger.error("Failed to save file on-demand: " + file.getName(), var3);
         }
      });
   }
}
