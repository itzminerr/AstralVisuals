package pl.astralvisuals.utils.client.text;

import net.minecraft.class_2561;
import net.minecraft.class_5250;
import pl.astralvisuals.utils.display.color.ColorAssist;

public class TextHelper {
   public static class_2561 applyGradient(String text, TextHelper.GradientStyle style, int color1, int color2, boolean bold) {
      switch (style) {
         case HALF_SPLIT:
            return halfSplitGradient(text, color1, color2, bold);
         case FULL_GRADIENT:
            return fullGradient(text, color1, color2, bold);
         case ASTOLFO:
            return astolfoGradient(text, bold);
         case TWO_COLOR_FADE:
            return twoColorFade(text, color1, color2, bold);
         default:
            return class_2561.method_43470(text).method_27694(s -> s.method_36139(color1).method_10982(bold));
      }
   }

   private static class_2561 halfSplitGradient(String text, int color1, int color2, boolean bold) {
      class_5250 result = class_2561.method_43470("");
      int midPoint = text.length() / 2;

      for (int i = 0; i < text.length(); i++) {
         int color = i < midPoint ? color1 : color2;
         result.method_10852(class_2561.method_43470(String.valueOf(text.charAt(i))).method_27694(s -> s.method_36139(color).method_10982(bold)));
      }

      return result;
   }

   private static class_2561 fullGradient(String text, int color1, int color2, boolean bold) {
      class_5250 result = class_2561.method_43470("");

      for (int i = 0; i < text.length(); i++) {
         float ratio = (float)i / (text.length() - 1);
         int color = ColorAssist.interpolate(color1, color2, ratio);
         result.method_10852(class_2561.method_43470(String.valueOf(text.charAt(i))).method_27694(s -> s.method_36139(color).method_10982(bold)));
      }

      return result;
   }

   private static class_2561 astolfoGradient(String text, boolean bold) {
      class_5250 result = class_2561.method_43470("");

      for (int i = 0; i < text.length(); i++) {
         int color = ColorAssist.astolfo(10, i, 0.7F, 0.7F, 1.0F);
         result.method_10852(class_2561.method_43470(String.valueOf(text.charAt(i))).method_27694(s -> s.method_36139(color).method_10982(bold)));
      }

      return result;
   }

   private static class_2561 twoColorFade(String text, int color1, int color2, boolean bold) {
      class_5250 result = class_2561.method_43470("");

      for (int i = 0; i < text.length(); i++) {
         float ratio = (float)i / (text.length() - 1);
         int color = ColorAssist.interpolateColor(color1, color2, ratio);
         result.method_10852(class_2561.method_43470(String.valueOf(text.charAt(i))).method_27694(s -> s.method_36139(color).method_10982(bold)));
      }

      return result;
   }

   public static class_2561 applyPredefinedGradient(String text, String gradientName, boolean bold) {
      String var3 = gradientName.toLowerCase();
      switch (var3) {
         case "red_blue":
            return applyGradient(text, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.red, ColorAssist.toColor("#0000FF"), bold);
         case "green_purple":
            return applyGradient(text, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.green, ColorAssist.toColor("#800080"), bold);
         case "yellow_cyan":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.yellow, ColorAssist.toColor("#00FFFF"), bold);
         case "orange_magenta":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.orange, ColorAssist.toColor("#FF00FF"), bold);
         case "astolfo":
            return applyGradient(text, TextHelper.GradientStyle.ASTOLFO, 0, 0, bold);
         case "blue_green_fade":
            return applyGradient(text, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#0000FF"), ColorAssist.green, bold);
         case "purple_red_fade":
            return applyGradient(text, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#800080"), ColorAssist.red, bold);
         case "cyan_orange_fade":
            return applyGradient(text, TextHelper.GradientStyle.TWO_COLOR_FADE, ColorAssist.toColor("#00FFFF"), ColorAssist.orange, bold);
         case "white_black":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForTextWhite$(), ColorAssist.colorForRectsBlack$(), bold);
         case "custom_purple":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForTextCustom$(), ColorAssist.colorForRectsCustom$(), bold);
         case "black_light_purple":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.colorForRectsBlack$(), ColorAssist.toColor("#DA70D6"), bold);
         case "dark_red_bright_red":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#8B0000"), ColorAssist.red, bold);
         case "dark_red":
            return applyGradient(text, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.toColor("#8B0000"), ColorAssist.toColor("#8B0000"), bold);
         case "red_white":
            return applyGradient(text, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.red, ColorAssist.colorForTextWhite$(), bold);
         case "purple_bright_pink":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#800080"), ColorAssist.toColor("#FF69B4"), bold);
         case "pink_dark_pink":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#FFC1CC"), ColorAssist.toColor("#C71585"), bold);
         case "bright_red":
            return applyGradient(text, TextHelper.GradientStyle.HALF_SPLIT, ColorAssist.red, ColorAssist.red, bold);
         case "dark_green_bright_green":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#006400"), ColorAssist.green, bold);
         case "red_orange":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.red, ColorAssist.orange, bold);
         case "turquoise_blue":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#40E0D0"), ColorAssist.toColor("#0000FF"), bold);
         case "light_cyan":
            return applyGradient(text, TextHelper.GradientStyle.FULL_GRADIENT, ColorAssist.toColor("#87CEEB"), ColorAssist.toColor("#00CED1"), bold);
         default:
            return class_2561.method_43470(text).method_27694(s -> s.method_36139(ColorAssist.colorForTextWhite$()).method_10982(bold));
      }
   }

   public static enum GradientStyle {
      HALF_SPLIT,
      FULL_GRADIENT,
      ASTOLFO,
      TWO_COLOR_FADE;
   }
}
