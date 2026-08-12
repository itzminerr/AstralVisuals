package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import pl.astralvisuals.commands.CommandDispatcher;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class PrefixCommand extends Command implements QuickImports {
   protected PrefixCommand() {
      super("prefix");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      if (arg.equals("set")) {
         args.requireMin(1);
         this.logDirect(
            "Установлен префикс '" + class_124.field_1061 + (CommandDispatcher.prefix = args.getString()) + class_124.field_1080 + "'", class_124.field_1080
         );
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny()) {
         String arg = args.getString();
         return arg.equalsIgnoreCase("set")
            ? new TabCompleteHelper().sortAlphabetically().prepend("name").stream()
            : new TabCompleteHelper().sortAlphabetically().prepend("set").filterPrefix(arg).stream();
      } else {
         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Позволяет менять префикс команд в моде";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно изменить префикс команд в моде", "", "Использование:", "> prefix set [название] - Устанавливает префикс команд"
      );
   }
}
