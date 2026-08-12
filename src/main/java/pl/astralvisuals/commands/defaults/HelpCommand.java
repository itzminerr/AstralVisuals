package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_5250;
import net.minecraft.class_2558.class_2559;
import net.minecraft.class_2568.class_5247;
import pl.astralvisuals.Force;
import pl.astralvisuals.commands.manager.CommandRepository;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.ICommand;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandNotFoundException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public class HelpCommand extends Command {
   Force main;

   protected HelpCommand(Force main) {
      super("help");
      this.main = main;
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      args.requireMax(1);
      CommandRepository commandRepository = this.main.getCommandRepository();
      if (args.hasAny() && !args.is(Integer.class)) {
         String commandName = args.getString().toLowerCase();
         ICommand command = commandRepository.getCommand(commandName);
         if (command == null) {
            throw new CommandNotFoundException(commandName);
         }

         this.logDirect("");
         command.getLongDesc().forEach(this::logDirect);
         this.logDirect("");
         class_5250 returnComponent = class_2561.method_43470("Нажмите что бы вернуться обратно в меню");
         returnComponent.method_10862(
            returnComponent.method_10866().method_10958(new class_2558(class_2559.field_11750, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label))
         );
         this.logDirect(returnComponent);
      } else {
         Paginator.paginate(
            args,
            new Paginator<>(commandRepository.getRegistry().descendingStream().filter(commandx -> !commandx.hiddenFromHelp()).collect(Collectors.toList())),
            () -> this.logDirect("Доступные команды:"),
            commandx -> {
               String names = String.join("/", commandx.getNames());
               String name = commandx.getNames().get(0);
               class_5250 shortDescComponent = class_2561.method_43470(" - " + commandx.getShortDesc());
               shortDescComponent.method_10862(shortDescComponent.method_10866().method_10977(class_124.field_1063));
               class_5250 namesComponent = class_2561.method_43470(names);
               namesComponent.method_10862(namesComponent.method_10866().method_10977(class_124.field_1068));
               class_5250 hoverComponent = class_2561.method_43470("");
               hoverComponent.method_10862(hoverComponent.method_10866().method_10977(class_124.field_1080));
               hoverComponent.method_10852(namesComponent);
               hoverComponent.method_27693("\n" + commandx.getShortDesc());
               hoverComponent.method_27693("\n\nНажмите, чтобы просмотреть полную справку о команде");
               String clickCommand = IBaritoneChatControl.FORCE_COMMAND_PREFIX + String.format("%s %s", label, commandx.getNames().get(0));
               class_5250 component = class_2561.method_43470(name);
               component.method_10862(component.method_10866().method_10977(class_124.field_1080));
               component.method_10852(shortDescComponent);
               component.method_10862(
                  component.method_10866()
                     .method_10949(new class_2568(class_5247.field_24342, hoverComponent))
                     .method_10958(new class_2558(class_2559.field_11750, clickCommand))
               );
               return component;
            },
            IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
         );
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      return args.hasExactlyOne()
         ? new TabCompleteHelper().addCommands(Force.getInstance().getCommandRepository()).filterPrefix(args.getString()).stream()
         : Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Просмотр всех доступных команд";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно просмотреть подробную справочную информацию о том, как использовать определенные команды",
         "",
         "Использование:",
         "> help - Перечисляет все команды и их краткие описания",
         "> help [команда] - Отображение справочной информации по конкретной команде"
      );
   }
}
