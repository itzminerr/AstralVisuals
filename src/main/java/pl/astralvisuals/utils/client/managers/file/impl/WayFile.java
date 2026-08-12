package pl.astralvisuals.utils.client.managers.file.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.class_2338;
import pl.astralvisuals.common.repository.way.Way;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.utils.client.managers.file.ClientFile;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;

public class WayFile extends ClientFile {
   static GsonBuilder GSON_BUILDER = new GsonBuilder().setPrettyPrinting();
   private final WayRepository wayRepository;

   public WayFile(WayRepository wayRepository) {
      super("Way");
      this.wayRepository = wayRepository;
   }

   @Override
   public void saveToFile(File path) throws FileSaveException {
      File file = new File(path, this.getName() + ".json");
      JsonArray ways = new JsonArray();

      for (Way way : this.wayRepository.wayList) {
         JsonObject object = new JsonObject();
         object.addProperty("name", way.name());
         object.addProperty("x", way.pos().method_10263());
         object.addProperty("y", way.pos().method_10264());
         object.addProperty("z", way.pos().method_10260());
         object.addProperty("server", way.server());
         ways.add(object);
      }

      try {
         try (FileWriter writer = new FileWriter(file)) {
            GSON_BUILDER.create().toJson(ways, writer);
         }
      } catch (IOException | JsonIOException var9) {
         throw new FileSaveException(String.format("Failed to save %s to file", this.getName()), var9);
      }
   }

   @Override
   public void loadFromFile(File path) throws FileLoadException {
      File file = new File(path, this.getName() + ".json");
      if (file.exists() && file.length() != 0L) {
         try {
            try (FileReader reader = new FileReader(file)) {
               JsonElement root = JsonParser.parseReader(reader);
               if (!root.isJsonArray()) {
                  return;
               }

               this.wayRepository.wayList.clear();

               for (JsonElement element : root.getAsJsonArray()) {
                  Way way = this.readWay(element);
                  if (way != null) {
                     this.wayRepository.wayList.add(way);
                  }
               }
            }
         } catch (IOException var10) {
            throw new FileLoadException(String.format("Failed to load %s from file", this.getName()), var10);
         } catch (JsonSyntaxException var11) {
            throw new FileLoadException(String.format("JSON syntax error, %s config cannot be loaded", this.getName()), var11);
         } catch (JsonIOException var12) {
            throw new FileLoadException(String.format("JSON IO error, %s config cannot be loaded", this.getName()), var12);
         }
      }
   }

   private Way readWay(JsonElement element) {
      if (!element.isJsonObject()) {
         return null;
      } else {
         JsonObject object = element.getAsJsonObject();
         String name = this.getString(object, "name", "");
         String server = this.getString(object, "server", "singleplayer");
         class_2338 pos = this.readPos(object);
         return !name.isBlank() && pos != null ? new Way(name, pos, server) : null;
      }
   }

   private class_2338 readPos(JsonObject object) {
      if (this.hasCoordinateSet(object)) {
         return new class_2338(this.getInt(object, "x", 0), this.getInt(object, "y", 0), this.getInt(object, "z", 0));
      } else {
         if (object.has("pos") && object.get("pos").isJsonObject()) {
            JsonObject pos = object.getAsJsonObject("pos");
            if (this.hasCoordinateSet(pos)) {
               return new class_2338(this.getInt(pos, "x", 0), this.getInt(pos, "y", 0), this.getInt(pos, "z", 0));
            }
         }

         return null;
      }
   }

   private boolean hasCoordinateSet(JsonObject object) {
      return object.has("x") && object.has("y") && object.has("z");
   }

   private String getString(JsonObject object, String key, String fallback) {
      return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
   }

   private int getInt(JsonObject object, String key, int fallback) {
      return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : fallback;
   }
}
