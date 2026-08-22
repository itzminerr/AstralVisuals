package pl.astralvisuals.utils.client.chat;

import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import pl.astralvisuals.utils.client.text.TextHelper;

public class ChatMessage {
   public static class_5250 brandmessage() {
      class_5250 result = class_2561.method_43470("[");
      result.method_10852(TextHelper.applyPredefinedGradient("ᴀsᴛʀᴀʟᴠɪsᴜᴀʟs", "light_cyan", true));
      result.method_10852(class_2561.method_43470("] "));
      result.method_10852(class_2561.method_43470("»").method_27694(s -> s.method_10977(class_124.field_1063)));
      return result;
   }

   public static void brandmessage(String message) {
      if (class_310.method_1551().field_1724 != null) {
         class_310.method_1551().field_1724.method_7353(brandmessageText(message), false);
      }
   }

   public static class_5250 brandmessageText(String message) {
      class_5250 prefix = class_2561.method_43470("[");
      prefix.method_10852(TextHelper.applyPredefinedGradient("ᴀsᴛʀᴀʟᴠɪsᴜᴀʟs", "light_cyan", true));
      prefix.method_10852(class_2561.method_43470("] "));
      prefix.method_10852(class_2561.method_43470("» ").method_27694(s -> s.method_10977(class_124.field_1063)));
      return prefix.method_27661().method_10852(class_2561.method_43470(message));
   }
}
