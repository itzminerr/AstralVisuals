package pl.astralvisuals.utils.client.managers.api.command.exception;

import java.util.List;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class CommandNotFoundException extends CommandException implements QuickLogger {
   public final String command;

   public CommandNotFoundException(String command) {
      super(String.format("Команда не найдена: %s", command));
      this.command = command;
   }

   @Override
   public void handle(ICommand command, List<ICommandArgument> args) {
      this.logDirect(this.getMessage());
   }
}
