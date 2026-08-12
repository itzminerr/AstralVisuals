package pl.astralvisuals.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import pl.astralvisuals.commands.CommandDispatcher;
import pl.astralvisuals.utils.client.managers.file.ClientFile;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;

public class PrefixFile extends ClientFile {
   public PrefixFile() {
      super("Prefix");
   }

   @Override
   public void saveToFile(File path) throws FileSaveException {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      File file = new File(path, this.getName() + ".json");

      try {
         try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(CommandDispatcher.prefix, writer);
         }
      } catch (IOException | JsonIOException var9) {
         throw new FileSaveException(String.format("Failed to save %s to file", this.getName()), var9);
      }
   }

   @Override
   public void loadFromFile(File path) throws FileLoadException {
      Gson gson = new Gson();
      File file = new File(path, this.getName() + ".json");
      if (file.exists() && file.length() != 0L) {
         try {
            try (FileReader reader = new FileReader(file)) {
               String string = (String)gson.fromJson(reader, String.class);
               CommandDispatcher.prefix = string != null && !string.isEmpty() ? string : ".";
            }
         } catch (IOException var9) {
            throw new FileLoadException(String.format("Failed to load %s from file", this.getName()), var9);
         } catch (JsonSyntaxException var10) {
            throw new FileLoadException(String.format("JSON syntax error, %s config cannot be loaded", this.getName()), var10);
         } catch (JsonIOException var11) {
            throw new FileLoadException(String.format("JSON IO error, %s config cannot be loaded", this.getName()), var11);
         }
      }
   }
}
