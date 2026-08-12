package pl.astralvisuals.utils.client.chat;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import pl.astralvisuals.utils.display.font.FontRenderer;
import pl.astralvisuals.utils.display.font.Fonts;
import pl.astralvisuals.utils.interactions.interact.PlayerInteractionHelper;

public final class StringHelper {
   public static String randomString(int length) {
      return IntStream.range(0, length).mapToObj(operand -> String.valueOf((char)new Random().nextInt(97, 123))).collect(Collectors.joining());
   }

   public static String getBindName(int key) {
      return key < 0
         ? "N/A"
         : PlayerInteractionHelper.getKeyType(key)
            .method_1447(key)
            .method_1441()
            .replace("key.keyboard.", "")
            .replace("key.mouse.", "mouse ")
            .replace(".", " ")
            .toUpperCase();
   }

   public static String wrap(String input, int width, int size) {
      String[] words = input.split(" ");
      StringBuilder output = new StringBuilder();
      float lineWidth = 0.0F;

      for (String word : words) {
         float wordWidth = Fonts.getSize(size).getStringWidth(word);
         if (lineWidth + wordWidth > width) {
            output.append("\n");
            lineWidth = 0.0F;
         } else if (lineWidth > 0.0F) {
            output.append(" ");
            lineWidth += Fonts.getSize(size).getStringWidth(" ");
         }

         output.append(word);
         lineWidth += wordWidth;
      }

      return output.toString();
   }

   public static String getDuration(int time) {
      int mins = time / 60;
      String sec = String.format("%02d", time % 60);
      return mins + ":" + sec;
   }

   public static String trimToWidth(String text, float maxWidth, FontRenderer font) {
      StringBuilder builder = new StringBuilder();

      for (char c : text.toCharArray()) {
         if (font.getStringWidth(builder.toString() + c) > maxWidth) {
            break;
         }

         builder.append(c);
      }

      return builder.toString();
   }

   private StringHelper() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
