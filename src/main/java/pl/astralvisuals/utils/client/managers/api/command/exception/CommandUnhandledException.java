package pl.astralvisuals.utils.client.managers.api.command.exception;

import java.util.List;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class CommandUnhandledException extends RuntimeException implements ICommandException, QuickLogger {
   public CommandUnhandledException(String message) {
      super(message);
   }

   public CommandUnhandledException(Throwable cause) {
      super(cause);
   }

   @Override
   public void handle(ICommand command, List<ICommandArgument> args) {
   }
}
