package pl.astralvisuals.utils.client.managers.api.command;

import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public interface ICommand extends QuickLogger {
   void execute(String var1, IArgConsumer var2) throws CommandException;

   Stream<String> tabComplete(String var1, IArgConsumer var2) throws CommandException;

   String getShortDesc();

   List<String> getLongDesc();

   List<String> getNames();

   default boolean hiddenFromHelp() {
      return false;
   }
}
