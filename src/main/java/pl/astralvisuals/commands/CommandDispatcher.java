package pl.astralvisuals.commands;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import pl.astralvisuals.Force;
import pl.astralvisuals.commands.argument.ArgConsumer;
import pl.astralvisuals.commands.argument.CommandArguments;
import pl.astralvisuals.commands.manager.CommandRepository;
import pl.astralvisuals.events.chat.ChatEvent;
import pl.astralvisuals.events.chat.TabCompleteEvent;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandNotEnoughArgumentsException;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandNotFoundException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.client.managers.api.command.manager.ICommandManager;
import pl.astralvisuals.utils.client.managers.event.EventHandler;
import pl.astralvisuals.utils.client.managers.event.EventManager;
import pl.astralvisuals.utils.display.interfaces.QuickLogger;

public class CommandDispatcher implements QuickLogger {
   private final ICommandManager manager = Force.getInstance().getCommandRepository();
   public static String prefix = ".";

   public CommandDispatcher(EventManager eventManager) {
      eventManager.register(this);
   }

   @EventHandler
   public void onChat(ChatEvent event) {
      String msg = event.getMessage();
      boolean forceRun = msg.startsWith(IBaritoneChatControl.FORCE_COMMAND_PREFIX);
      if (msg.startsWith(prefix) || forceRun) {
         event.cancel();
         String commandStr = msg.substring(forceRun ? IBaritoneChatControl.FORCE_COMMAND_PREFIX.length() : prefix.length());
         if (!this.runCommand(commandStr) && !commandStr.trim().isEmpty()) {
            new CommandNotFoundException((String)CommandRepository.expand(commandStr).method_15442()).handle(null, null);
         }
      }
   }

   public boolean runCommand(String msg) {
      if (msg.isEmpty()) {
         return this.runCommand("help");
      } else {
         class_3545<String, List<ICommandArgument>> pair = CommandRepository.expand(msg);
         String command = (String)pair.method_15442();
         String rest = msg.substring(((String)pair.method_15442()).length());
         new ArgConsumer(this.manager, (List<ICommandArgument>)pair.method_15441());
         return this.manager.execute(pair);
      }
   }

   @EventHandler
   public void onTabComplete(TabCompleteEvent event) {
      String eventPrefix = event.prefix;
      if (eventPrefix.startsWith(prefix)) {
         String msg = eventPrefix.substring(prefix.length());
         List<ICommandArgument> args = CommandArguments.from(msg, true);
         Stream<String> stream = this.tabComplete(msg);
         if (args.size() == 1) {
            stream = stream.map(x -> prefix + x);
         }

         event.completions = stream.toArray(String[]::new);
      }
   }

   public Stream<String> tabComplete(String msg) {
      try {
         List<ICommandArgument> args = CommandArguments.from(msg, true);
         ArgConsumer argc = new ArgConsumer(this.manager, args);
         return argc.hasAtMost(2) && argc.hasExactly(1)
            ? new TabCompleteHelper().addCommands(this.manager).filterPrefix(argc.getString()).stream()
            : this.manager.tabComplete(msg);
      } catch (CommandNotEnoughArgumentsException var4) {
         return Stream.empty();
      }
   }
}
