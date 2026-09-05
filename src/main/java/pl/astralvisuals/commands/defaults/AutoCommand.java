package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.autocommand.AutoCommandEntry;
import pl.astralvisuals.common.repository.autocommand.AutoCommandRepository;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public class AutoCommand extends Command {
   private final AutoCommandRepository autoCommandRepository;

   public AutoCommand(Force main) {
      super("auto");
      this.autoCommandRepository = main.getAutoCommandRepository();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAdd(args);
            break;
         case "remove":
            this.handleRemove(args);
            break;
         case "list":
            this.handleList(args, label);
            break;
         case "clear":
            this.handleClear(args);
            break;
         case "stop":
            this.handleStop(args);
            break;
         case "start":
            this.handleStart(args);
            break;
         default:
            throw new CommandException("Неизвестное действие: " + action);
      }
   }

   private void handleAdd(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String name = args.getString();
      String raw = args.rawRest().trim();
      if (raw.isEmpty()) {
         throw new CommandException("Формат: auto add [название] [команда] [задержка в секундах]");
      }

      String command;
      String delayStr;
      if (raw.startsWith("\"")) {
         int close = raw.indexOf('"', 1);
         if (close == -1) {
            throw new CommandException("Незакрытая кавычка: команда должна заканчиваться на \"");
         }
         command = raw.substring(1, close);
         delayStr = raw.substring(close + 1).trim();
      } else {
         int lastSpace = raw.lastIndexOf(' ');
         if (lastSpace == -1) {
            command = "";
            delayStr = raw;
         } else {
            command = raw.substring(0, lastSpace).trim();
            delayStr = raw.substring(lastSpace + 1).trim();
         }
      }

      if (command.isEmpty()) {
         throw new CommandException("Команда не может быть пустой");
      }

      int delaySeconds;
      try {
         delaySeconds = Integer.parseInt(delayStr);
      } catch (NumberFormatException var11) {
         throw new CommandException("Задержка должна быть числом (секунды)");
      }
      if (delaySeconds <= 0) {
         throw new CommandException("Задержка должна быть больше нуля");
      }

      if (this.autoCommandRepository.hasEntry(name)) {
         this.logDirect("Автокоманда с таким названием уже есть", class_124.field_1061);
      } else {
         this.autoCommandRepository.addEntry(name, command, delaySeconds);
         this.logDirect(
            class_124.field_1060
               + "Добавлена автокоманда "
               + class_124.field_1061
               + name
               + class_124.field_1060
               + " с командой "
               + class_124.field_1061
               + command
               + class_124.field_1060
               + " каждые "
               + class_124.field_1061
               + delaySeconds
               + " сек."
         );
      }
   }

   private void handleRemove(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      String name = args.getString();
      if (this.autoCommandRepository.removeEntry(name)) {
         this.logDirect(class_124.field_1060 + "Автокоманда " + class_124.field_1061 + name + class_124.field_1060 + " удалена");
      } else {
         this.logDirect("Автокоманда с таким названием не найдена", class_124.field_1061);
      }
   }

   private void handleList(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<AutoCommandEntry> entries = this.autoCommandRepository.entries;
      Paginator.paginate(
         args,
         new Paginator<>(entries),
         () -> this.logDirect("Список автокоманд" + (this.autoCommandRepository.isStopped() ? " (остановлены)" : ":")),
         entry -> {
            String names = entry.name();
            String command = entry.command();
            String delay = String.valueOf(entry.delayMs() / 1000L);
            return class_2561.method_43470(class_124.field_1080 + "Название: " + class_124.field_1068 + names)
               .method_10852(class_2561.method_43470(class_124.field_1080 + " Команда: " + class_124.field_1068 + command))
               .method_10852(class_2561.method_43470(class_124.field_1080 + " Каждые: " + class_124.field_1068 + delay + " сек."));
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClear(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.autoCommandRepository.clearEntry();
      this.logDirect("Все автокоманды удалены", class_124.field_1060);
   }

   private void handleStop(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.autoCommandRepository.stop();
      this.logDirect("Все автокоманды остановлены", class_124.field_1060);
   }

   private void handleStart(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.autoCommandRepository.start();
      this.logDirect("Все автокоманды запущены", class_124.field_1060);
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny()) {
         String first = args.getString().toLowerCase(Locale.US);
         if (first.equals("remove")) {
            String rest = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "";
            return new TabCompleteHelper()
               .append(this.autoCommandRepository.entries.stream().map(AutoCommandEntry::name))
               .filterPrefix(rest)
               .sortAlphabetically()
               .stream();
         }

         if (args.hasExactlyOne()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear", "stop", "start").filterPrefix(first).stream();
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Автоматический повтор команд с заданной задержкой";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Эта команда позволяет автоматически повторять заданные команды в чат с заданным интервалом. Можно запускать несколько автокоманд одновременно.",
         "",
         "Использование:",
         "> auto add [название] [команда] [задержка в секундах] - Создаёт автокоманду, которая вводит указанную команду каждые N секунд",
         "> auto add [название] \"[команда]\" [задержка] - То же, но команду можно взять в кавычки, чтобы в ней были пробелы или число в конце (например: auto add sell \"/market sell 30\" 5)",
         "> auto stop - Останавливает все автокоманды",
         "> auto start - Возобновляет работу всех автокоманд",
         "> auto list - Отображает список всех автокоманд",
         "> auto remove [название] - Удаляет автокоманду с указанным названием",
         "> auto clear - Удаляет все автокоманды"
      );
   }
}
