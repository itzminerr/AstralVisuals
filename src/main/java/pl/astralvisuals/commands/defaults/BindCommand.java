package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.features.module.ModuleProvider;
import pl.astralvisuals.features.module.ModuleRepository;
import pl.astralvisuals.utils.client.chat.StringHelper;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.KeyDataType;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.ModuleDataType;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandNotEnoughArgumentsException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public class BindCommand extends Command {
   private final ModuleProvider moduleProvider;
   private final ModuleRepository moduleRepository;

   public BindCommand(Force main) {
      super("bind");
      this.moduleRepository = main.getModuleRepository();
      this.moduleProvider = main.getModuleProvider();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String action = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (action) {
         case "add":
            this.handleAddBind(args);
            break;
         case "remove":
            this.handleRemoveBind(args);
            break;
         case "list":
            this.handleListBinds(args, label);
            break;
         case "clear":
            this.handleClearBinds(args);
            break;
         case "set":
            this.handleSetBind(args);
      }
   }

   private void handleSetBind(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String target = args.getString().toLowerCase(Locale.US);
      if (target.equals("clickgui")) {
         int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
         BindCommand.ClickGuiManager.setClickGuiKey(key);
         this.logDirect(
            class_124.field_1060 + "Клавиша для открытия ClickGUI изменена на: " + class_124.field_1061 + StringHelper.getBindName(key).toLowerCase()
         );
      } else {
         throw new CommandException("Неизвестная цель для установки бинда: " + target);
      }
   }

   private void handleAddBind(IArgConsumer args) throws CommandException {
      args.requireMin(2);
      String moduleName = args.getString();
      int key = args.<Entry<String, Integer>, KeyDataType>getDatatypeFor(KeyDataType.INSTANCE).getValue();
      Module module = this.moduleProvider.get(moduleName);
      module.setKey(key);
      this.logDirect(
         class_124.field_1060
            + "Модуль "
            + class_124.field_1061
            + moduleName
            + class_124.field_1060
            + " привязан к кнопке "
            + class_124.field_1061
            + StringHelper.getBindName(key).toLowerCase()
      );
   }

   private void handleRemoveBind(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      String moduleName = args.getString();
      Module module = this.moduleProvider.get(moduleName);
      module.setKey(-1);
      this.logDirect(class_124.field_1060 + "Бинд для модуля " + class_124.field_1061 + moduleName + class_124.field_1060 + " был успешно удален");
   }

   private void handleListBinds(IArgConsumer args, String label) throws CommandException {
      args.requireMax(1);
      List<Module> filtredList = this.moduleRepository.modules().stream().filter(module -> module.getKey() != -1).toList();
      Paginator.paginate(
         args,
         new Paginator<>(filtredList),
         () -> this.logDirect("Список модулей:"),
         module -> {
            String names = module.getName();
            String keys = StringHelper.getBindName(module.getKey()).toLowerCase();
            return class_2561.method_43470(class_124.field_1080 + "Название: " + class_124.field_1068 + names)
               .method_10852(class_2561.method_43470(class_124.field_1080 + " Клавиша: " + class_124.field_1068 + keys));
         },
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearBinds(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.moduleRepository.modules().forEach(function -> function.setKey(-1));
      this.logDirect("Все бинды модулей были удалены", class_124.field_1060);
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasExactlyOne()) {
         return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear", "set").filterPrefix(args.getString()).stream();
      } else {
         String arg = args.getString();
         if (arg.equalsIgnoreCase("add")) {
            if (args.hasExactly(1)) {
               return args.tabCompleteDatatype(ModuleDataType.INSTANCE);
            }

            if (args.hasExactly(2)) {
               return args.tabCompleteDatatype(KeyDataType.INSTANCE);
            }
         } else if (arg.equalsIgnoreCase("set")) {
            if (args.hasExactly(1)) {
               return Stream.of("clickgui").filter(s -> {
                  try {
                     return s.startsWith(args.getString().toLowerCase(Locale.US));
                  } catch (CommandNotEnoughArgumentsException var3x) {
                     throw new RuntimeException(var3x);
                  }
               });
            }

            if (args.hasExactly(2)) {
               return args.tabCompleteDatatype(KeyDataType.INSTANCE);
            }
         }

         return Stream.empty();
      }
   }

   @Override
   public String getShortDesc() {
      return "Управление биндами для модулей и GUI";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "Эта команда позволяет управлять биндами для модулей и GUI, которые будут активироваться при нажатии определённых клавиш",
         "",
         "Использование:",
         "> bind add [модуль] [клавиша] - Привязывает модуль к указанной клавише",
         "> bind remove [модуль] - Удаляет привязку модуля",
         "> bind list - Показывает список всех текущих биндов модулей",
         "> bind clear - Удаляет все бинды модулей",
         "> bind set clickgui [клавиша] - Изменяет клавишу для открытия ClickGUI"
      );
   }

   public class ClickGuiManager {
      public static int clickGuiKey = 344;

      public static void setClickGuiKey(int key) {
         clickGuiKey = key;
      }

      public static int getClickGuiKey() {
         return clickGuiKey;
      }
   }
}
