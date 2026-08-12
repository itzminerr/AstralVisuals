package pl.astralvisuals.utils.client.managers.api.command.manager;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.client.managers.api.command.registry.Registry;

public interface ICommandManager {
   Registry<ICommand> getRegistry();

   ICommand getCommand(String var1);

   boolean execute(String var1);

   boolean execute(class_3545<String, List<ICommandArgument>> var1);

   Stream<String> tabComplete(class_3545<String, List<ICommandArgument>> var1);

   Stream<String> tabComplete(String var1);
}
