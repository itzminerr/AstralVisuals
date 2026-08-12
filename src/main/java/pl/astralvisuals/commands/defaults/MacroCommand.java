package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.macro.MacroRepository;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.KeyDataType;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.MacroDataType;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public class MacroCommand extends Command {
   private final MacroRepository macroRepository;

   public MacroCommand(Force main) {
      super("macro", "macros");
      this.macroRepository = main.getMacroRepository();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddMacro(args);
            break;
         case "remove":
            this.handleRemoveMacro(args);
            break;
         case "list":
            this.handleListMacros(args, label);
            break;
         case "clear":
            this.handleClearMacros(args);
      }
   }

   private void handleAddMacro(IArgConsumer args) throws CommandException {
      args.requireMin(3);
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      String name = args.getString();
      String command = args.rawRest();
      if (this.macroRepository.hasMacro(name)) {
         this.logDirect("Макрос с таким именем уже есть в списке", class_124.field_1061);
      } else {
         this.macroRepository.addMacro(name, command, key);
         this.logDirect(
            class_124.field_1060
               + "Добавлен макрос с названием "
               + class_124.field_1061
               + name
               + class_124.field_1060
               + " с кнопкой "
               + class_124.field_1061
               + StringHelper.getBindName(key).toLowerCase()
               + class_124.field_1060
               + " с командой "
               + class_124.field_1061
               + command
         );
      }
   }

   private void handleRemoveMacro(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      String name = args.getString();
      if (this.macroRepository.hasMacro(name)) {
         this.macroRepository.deleteMacro(name);
         this.logDirect(class_124.field_1060 + "Макрос " + class_124.field_1061 + name + class_124.field_1060 + " был успешно удален");
      } else {
         this.logDirect("Макрос с таким именем не найден", class_124.field_1061);
      }
   }

   private void handleListMacros(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      Paginator.paginate(
         args,
         new Paginator<>(this.macroRepository.macroList),
         () -> this.logDirect("Список макросов:"),
         macro -> {
            String names = macro.name();
            String keys = StringHelper.getBindName(macro.key()).toLowerCase();
            String command = macro.message();
            return class_2561.method_43470(class_124.field_1080 + "Название: " + class_124.field_1068 + names)
               .method_10852(class_2561.method_43470(class_124.field_1080 + " Клавиша: " + class_124.field_1068 + keys))
               .method_10852(class_2561.method_43470(class_124.field_1080 + " Команда: " + class_124.field_1068 + command));
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearMacros(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.macroRepository.clearList();
      this.logDirect("Все макросы были удалены", class_124.field_1060);
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny() && args.hasExactlyOne()) {
         return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(args.getString()).stream();
      } else {
         if (args.hasAny()) {
            String arg = args.getString();
            if (arg.equalsIgnoreCase("add") && args.hasExactlyOne()) {
               return args.tabCompleteDatatype(KeyDataType.INSTANCE);
            }

            if (arg.equalsIgnoreCase("remove") && args.hasExactlyOne()) {
               return args.tabCompleteDatatype(MacroDataType.INSTANCE);
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Позволяет управлять макросами";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Эта команда позволяет управлять макросами, которые автоматически вводят заданные команды в чат",
         "",
         "Использование:",
         "> macro add [клавиша] [название] [сообщение] - Добавляет новый макрос, который будет активироваться при нажатии на указанную клавишу и вводить указанное сообщение",
         "> macro remove [название] - Удаляет макрос с указанным именем",
         "> macro list - Отображает список всех текущих макросов",
         "> macro clear - Удаляет все макросы из списка"
      );
   }
}
