package pl.astralvisuals.utils.client.managers.api.command.exception;

import java.util.List;
import net.minecraft.class_124;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public interface ICommandException extends QuickLogger {
   String getMessage();

   default void handle(ICommand command, List<ICommandArgument> args) {
      this.logDirect(this.getMessage(), class_124.field_1061);
   }
}
