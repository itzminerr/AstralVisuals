package pl.astralvisuals.commands.defaults;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import pl.astralvisuals.Force;
import pl.astralvisuals.main.client.ClientInfoProvider;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.ConfigFileDataType;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.client.managers.file.exception.FileLoadException;
import pl.astralvisuals.utils.client.managers.file.exception.FileSaveException;
import pl.astralvisuals.utils.client.managers.file.impl.ModuleFile;

public class ConfigCommand extends Command {
   private final ClientInfoProvider clientInfoProvider;
   private final ModuleFile moduleFile;

   protected ConfigCommand(Force main) {
      super("config", "cfg");
      this.clientInfoProvider = main.getClientInfoProvider();
      this.moduleFile = new ModuleFile(main.getModuleRepository(), main.getDraggableRepository());
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      args.requireMax(1);
      if (arg.contains("load")) {
         String name = args.getString();
         File customDir = this.getCustomDir();
         File configFile = new File(customDir, name + ".json");
         if (!configFile.exists()) {
            this.logDirect(String.format("Конфигурация %s не найдена", name), class_124.field_1061);
            return;
         }

         try {
            this.moduleFile.loadFromFile(customDir, name + ".json");
            this.logDirect(String.format("Конфигурация %s загружена", name));
         } catch (FileLoadException var10) {
            this.logDirect(String.format("Ошибка при загрузке конфига, детали: %s", var10.getMessage()), class_124.field_1061);
         }
      }

      if (arg.contains("save")) {
         String name = args.getString();

         try {
            this.moduleFile.saveToFile(this.getCustomDir(), name + ".json");
            this.logDirect(String.format("Конфигурация %s сохранена", name));
         } catch (FileSaveException var9) {
            this.logDirect(String.format("Ошибка при сохранении конфига, детали: %s", var9.getMessage()), class_124.field_1061);
         }
      }

      if (arg.contains("list")) {
         Paginator.paginate(args, new Paginator<>(this.getConfigs()), () -> this.logDirect("Список конфигов:"), config -> {
            class_5250 namesComponent = class_2561.method_43470(config);
            namesComponent.method_10862(namesComponent.method_10866().method_10977(class_124.field_1068));
            return namesComponent;
         }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
      }

      if (arg.contains("dir")) {
         try {
            Runtime.getRuntime().exec("explorer " + this.getCustomDir().getAbsolutePath());
         } catch (IOException var8) {
            this.logDirect("Папка с конфигами не найдена " + var8.getMessage(), class_124.field_1061);
         }
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny()) {
         String arg = args.getString();
         if (!args.hasExactlyOne()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("load", "save", "list", "dir").filterPrefix(arg).stream();
         }

         if (arg.equalsIgnoreCase("load")) {
            return args.tabCompleteDatatype(ConfigFileDataType.INSTANCE);
         }

         if (arg.equalsIgnoreCase("save")) {
            return args.tabCompleteDatatype(ConfigFileDataType.INSTANCE);
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Позволяет взаимодействовать с конфигами клиента";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно загружать и сохранять конфиги",
         "",
         "Использование:",
         "> config load [название] - Загружает конфиг",
         "> config sav [название] - Сохраняет конфиг",
         "> config list - Показывает список конфигов",
         "> config dir - Открывает папку с конфигами"
      );
   }

   public List<String> getConfigs() {
      List<String> configs = new ArrayList<>();
      File[] configFiles = this.getCustomDir().listFiles();
      if (configFiles != null) {
         for (File configFile : configFiles) {
            if (configFile.isFile() && configFile.getName().endsWith(".json")) {
               configs.add(configFile.getName().replace(".json", ""));
            }
         }
      }

      return configs;
   }

   private File getCustomDir() {
      File customDir = new File(this.clientInfoProvider.clientDir(), "Custom");
      if (!customDir.exists()) {
         customDir.mkdirs();
      }

      return customDir;
   }
}
