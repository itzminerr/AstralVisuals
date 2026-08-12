package pl.astralvisuals.utils.display.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import pl.astralvisuals.Force;

public class Fonts {
   private static final Map<Fonts.FontKey, FontRenderer> fontCache = new HashMap<>();

   public static FontRenderer create(float size, String name) {
      try {
         String ttfPath = "assets/minecraft/fonts/" + name + ".ttf";
         String otfPath = "assets/minecraft/fonts/" + name + ".otf";
         InputStream resourceStream = Force.class.getClassLoader().getResourceAsStream(ttfPath);
         if (resourceStream == null) {
            resourceStream = Force.class.getClassLoader().getResourceAsStream(otfPath);
         }

         FontRenderer var7;
         try (InputStream inputStream = Objects.requireNonNull(resourceStream)) {
            Font font = Font.createFont(0, inputStream).deriveFont(size / 2.0F);
            var7 = new FontRenderer(font, size / 2.0F);
         }

         return var7;
      } catch (Exception var10) {
         throw new IllegalStateException("Unable to load font " + name, var10);
      }
   }

   public static void init() {
      for (Fonts.Type type : Fonts.Type.values()) {
         for (int size = 4; size <= 32; size++) {
            fontCache.put(new Fonts.FontKey(size, type), create(size, type.getType()));
         }
      }
   }

   public static FontRenderer getSize(int size) {
      return getSize(size, Fonts.Type.INST);
   }

   public static FontRenderer getSize(int size, Fonts.Type type) {
      return fontCache.computeIfAbsent(new Fonts.FontKey(size, type), k -> create(size, type.getType()));
   }

   private record FontKey(int size, Fonts.Type type) {
   }

   public static enum Type {
      DEFAULT("sf_medium"),
      REGULAR("sf_regular"),
      SEMI("sf_semibold"),
      BOLD("sf_bold"),
      BOLDED("bold"),
      MANROPEEXTRABOLD("manropeextrabold"),
      MANROPEBOLD("manropebold"),
      ZENITH_DEFAULT("sfpromedium"),
      ZENITH_BOLD("sfprosemibold"),
      HUD("icon"),
      GUI("apexgui"),
      INST("suisseintl"),
      ICONS("icons"),
      ICONSTYPENEW("icon2"),
      GUIICONS("guiicons"),
      ICONSCATEGORY("categoryicons");

      private final String type;

      public String getType() {
         return this.type;
      }

      private Type(final String type) {
         this.type = type;
      }
   }
}
