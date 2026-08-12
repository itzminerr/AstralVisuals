package pl.astralvisuals.utils.features.price;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1799;
import net.minecraft.class_9323;
import org.apache.commons.lang3.StringUtils;

public class PriceParser {
   private final Pattern funTimePricePattern = Pattern.compile("\\$(\\d+(?:\\s\\d{3})*(?:\\.\\d{2})?)");

   public int getPrice(class_1799 stack) {
      class_9323 tag = stack.method_57353();
      if (tag == null) {
         return -1;
      } else {
         String componentString = tag.toString();
         String price = StringUtils.substringBetween(componentString, "literal{ $", "}[style={color=green}]");
         if (price == null || price.isEmpty()) {
            String customName = stack.method_7964().getString();
            if (customName != null) {
               Matcher matcher = this.funTimePricePattern.matcher(customName);
               if (matcher.find()) {
                  price = matcher.group(1);
               }
            }
         }

         if (price != null && !price.isEmpty()) {
            try {
               price = price.replaceAll("[\\s,]", "");
               return Integer.parseInt(price);
            } catch (NumberFormatException var7) {
               return -1;
            }
         } else {
            return -1;
         }
      }
   }
}
