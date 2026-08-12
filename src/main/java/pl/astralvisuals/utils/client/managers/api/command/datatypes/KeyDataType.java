package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum KeyDataType implements IDatatypeFor<java.util.Map.Entry<String, Integer>> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> keys = getKeys().keySet().stream();
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(keys).filterPrefix(context).sortAlphabetically().stream();
   }

   public java.util.Map.Entry<String, Integer> get(IDatatypeContext datatypeContext) throws CommandException {
      String key = datatypeContext.getConsumer().getString();
      return getKeys().entrySet().stream().filter(s -> s.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
   }

   private static Map<String, Integer> getKeys() {
      Map<String, Integer> keys = new HashMap<>();
      ObjectIterator var1 = class_307.field_1668.field_1674.int2ObjectEntrySet().iterator();

      while (var1.hasNext()) {
         Entry<class_306> entry = (Entry<class_306>)var1.next();
         int keyCode = entry.getIntKey();
         String bindName = StringHelper.getBindName(keyCode).toLowerCase();
         keys.put(bindName, keyCode);
      }

      return keys;
   }
}
