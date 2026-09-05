package pl.astralvisuals.commands.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;

public final class DefaultCommands {
   public static List<ICommand> createAll() {
      Force main = Force.getInstance();
      List<ICommand> commands = new ArrayList<>(
         Arrays.asList(
            new ConfigCommand(main),
            new MacroCommand(main),
            new AutoCommand(main),
            new HelpCommand(main),
            new BindCommand(main),
            new WayCommand(main),
            new FriendCommand(),
            new PrefixCommand()
         )
      );
      return Collections.unmodifiableList(commands);
   }
}
