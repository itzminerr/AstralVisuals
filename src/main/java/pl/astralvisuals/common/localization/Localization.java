package pl.astralvisuals.common.localization;

import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2960;
import net.minecraft.class_3298;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class Localization implements QuickImports {
   private static final Map<Language, Map<String, String>> cache = new ConcurrentHashMap<>();

   public static String get(String key) {
      Map<String, String> translations = cache.computeIfAbsent(Language.ENG, Localization::loadTranslations);
      return translations.getOrDefault(key, key);
   }

   private static Map<String, String> loadTranslations(Language language) {
      try {
         class_2960 identifier = class_2960.method_60654("translations/" + language.getFile() + ".json");
         InputStream stream = ((class_3298)mc.method_1478().method_14486(identifier).get()).method_14482();
         BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
         return (Map<String, String>)gson.fromJson(reader, (new TypeToken<Map<String, String>>() {}).getType());
      } catch (Exception var4) {
         throw new IllegalStateException("Unable to load translations for " + language, var4);
      }
   }
}
