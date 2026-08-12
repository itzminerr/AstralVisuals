package pl.astralvisuals.utils.display.interfaces;

import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import pl.astralvisuals.utils.client.chat.ChatMessage;

public interface QuickLogger {
   static class_2561 getPrefix() {
      class_5250 text = ChatMessage.brandmessage();
      text.method_10862(text.method_10866().method_10977(class_124.field_1080));
      return text;
   }

   default void logDirect(class_2561... components) {
      class_5250 component = class_2561.method_43470("");
      component.method_10852(getPrefix());
      component.method_10852(class_2561.method_43470(" "));
      Arrays.asList(components).forEach(component::method_10852);
      if (class_310.method_1551().field_1724 != null) {
         class_310.method_1551().field_1705.method_1743().method_1812(component);
      }
   }

   default void logDirect(String message, class_124 color) {
      Stream.of(message.split("\n")).forEach(line -> {
         class_5250 component = class_2561.method_43470(line.replace("\t", "    "));
         component.method_10862(component.method_10866().method_10977(color));
         this.logDirect(component);
      });
   }

   default void logDirect(String message) {
      this.logDirect(message, class_124.field_1080);
   }
}
