package pl.astralvisuals.commands.manager;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_3545;
import pl.astralvisuals.Force;
import pl.astralvisuals.commands.argument.ArgConsumer;
import pl.astralvisuals.commands.argument.CommandArguments;
import pl.astralvisuals.commands.defaults.DefaultCommands;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.ICommandArgument;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandUnhandledException;
import pl.astralvisuals.utils.client.managers.api.command.exception.ICommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.client.managers.api.command.manager.ICommandManager;
import pl.astralvisuals.utils.client.managers.api.command.registry.Registry;

public class CommandRepository implements ICommandManager {
   private final Registry<ICommand> registry = new Registry<>();

   public CommandRepository() {
      DefaultCommands.createAll().forEach(this.registry::register);
   }

   @Override
   public Registry<ICommand> getRegistry() {
      return this.registry;
   }

   @Override
   public ICommand getCommand(String name) {
      for (ICommand command : this.registry.entries) {
         if (command.getNames().contains(name.toLowerCase(Locale.US))) {
            return command;
         }
      }

      return null;
   }

   @Override
   public boolean execute(String string) {
      return this.execute(expand(string));
   }

   @Override
   public boolean execute(class_3545<String, List<ICommandArgument>> expanded) {
      CommandRepository.ExecutionWrapper execution = this.from(expanded);
      if (execution != null) {
         execution.execute();
      }

      return execution != null;
   }

   @Override
   public Stream<String> tabComplete(class_3545<String, List<ICommandArgument>> expanded) {
      CommandRepository.ExecutionWrapper execution = this.from(expanded);
      return execution == null ? Stream.empty() : execution.tabComplete();
   }

   @Override
   public Stream<String> tabComplete(String prefix) {
      class_3545<String, List<ICommandArgument>> pair = expand(prefix, true);
      String label = (String)pair.method_15442();
      List<ICommandArgument> args = (List<ICommandArgument>)pair.method_15441();
      return args.isEmpty()
         ? new TabCompleteHelper().addCommands(Force.getInstance().getCommandRepository()).filterPrefix(label).stream()
         : this.tabComplete(pair);
   }

   private CommandRepository.ExecutionWrapper from(class_3545<String, List<ICommandArgument>> expanded) {
      String label = (String)expanded.method_15442();
      ArgConsumer args = new ArgConsumer(this, (List<ICommandArgument>)expanded.method_15441());
      ICommand command = this.getCommand(label);
      return command == null ? null : new CommandRepository.ExecutionWrapper(command, label, args);
   }

   private static class_3545<String, List<ICommandArgument>> expand(String string, boolean preserveEmptyLast) {
      String label = string.split("\\s", 2)[0];
      List<ICommandArgument> args = CommandArguments.from(string.substring(label.length()), preserveEmptyLast);
      return new class_3545(label, args);
   }

   public static class_3545<String, List<ICommandArgument>> expand(String string) {
      return expand(string, false);
   }

   private static final class ExecutionWrapper {
      private ICommand command;
      private String label;
      private ArgConsumer args;

      private ExecutionWrapper(ICommand command, String label, ArgConsumer args) {
         this.command = command;
         this.label = label;
         this.args = args;
      }

      private void execute() {
         try {
            this.command.execute(this.label, this.args);
         } catch (Throwable var3) {
            ICommandException exception = (ICommandException)(var3 instanceof ICommandException ? (ICommandException)var3 : new CommandUnhandledException(var3));
            exception.handle(this.command, this.args.getArgs());
         }
      }

      private Stream<String> tabComplete() {
         try {
            return this.command.tabComplete(this.label, this.args);
         } catch (CommandException var2) {
         } catch (Throwable var3) {
            var3.printStackTrace();
         }

         return Stream.empty();
      }
   }
}
