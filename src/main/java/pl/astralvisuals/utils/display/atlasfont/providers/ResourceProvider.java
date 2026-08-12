package pl.astralvisuals.utils.display.atlasfont.providers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3300;

public final class ResourceProvider {
   private static final class_3300 RESOURCE_MANAGER = class_310.method_1551().method_1478();
   private static final Gson GSON = new Gson();

   public static class_2960 getShaderIdentifier(String name) {
      return class_2960.method_60655("mre", "core/" + name);
   }

   public static JsonObject toJson(class_2960 identifier) {
      return JsonParser.parseString(toString(identifier)).getAsJsonObject();
   }

   public static <T> T fromJsonToInstance(class_2960 identifier, Class<T> clazz) {
      return (T)GSON.fromJson(toString(identifier), clazz);
   }

   public static String toString(class_2960 identifier) {
      return toString(identifier, "\n");
   }

   public static String toString(class_2960 identifier, String delimiter) {
      try {
         String var4;
         try (
            InputStream inputStream = RESOURCE_MANAGER.open(identifier);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         ) {
            var4 = reader.lines().collect(Collectors.joining(delimiter));
         }

         return var4;
      } catch (IOException var10) {
         throw new RuntimeException(var10);
      }
   }
}
