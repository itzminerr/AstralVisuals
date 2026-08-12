package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.way.WayRepository;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.WayDataType;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public class WayCommand extends Command implements QuickImports {
   private final WayRepository wayRepository;

   protected WayCommand(Force main) {
      super("way");
      this.wayRepository = main.getWayRepository();
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      switch (arg) {
         case "add":
            this.handleAddWay(args);
            break;
         case "remove":
            this.handleRemoveWay(args);
            break;
         case "clear":
            this.handleClearWays(args);
            break;
         case "list":
            this.handleListWays(label, args);
      }
   }

   private void handleAddWay(IArgConsumer args) throws CommandException {
      args.requireMin(1);
      String name = args.getString();
      class_2338 pos;
      if (args.hasAny()) {
         args.requireExactly(3);
         pos = new class_2338(args.getAs(Integer.class), args.getAs(Integer.class), args.getAs(Integer.class));
      } else {
         if (mc.field_1724 == null) {
            this.logDirect("Зайди в мир, чтобы поставить метку на текущем месте", class_124.field_1061);
            return;
         }

         pos = mc.field_1724.method_24515();
      }

      if (this.wayRepository.hasWay(name)) {
         this.logDirect("Метка с таким именем уже есть в списке", class_124.field_1061);
      } else {
         String address = this.wayRepository.getCurrentServerKey();
         this.logDirect(
            "Добавлена метка "
               + name
               + ", координаты: ("
               + pos.method_10263()
               + ", "
               + pos.method_10264()
               + ", "
               + pos.method_10260()
               + "), сервер: "
               + address,
            class_124.field_1080
         );
         this.wayRepository.addWay(name, pos, address);
      }
   }

   private void handleRemoveWay(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      String name = args.getString();
      if (this.wayRepository.hasWay(name)) {
         this.wayRepository.deleteWay(name);
         this.logDirect(class_124.field_1060 + "Метка " + class_124.field_1061 + name + class_124.field_1060 + " была успешна удалена");
      } else {
         this.logDirect("Метка с названием '" + name + "' не найдена");
      }
   }

   private void handleListWays(String label, IArgConsumer args) throws CommandException {
      args.requireMax(1);
      Paginator.paginate(
         args,
         new Paginator<>(this.wayRepository.wayList),
         () -> this.logDirect("Список меток:"),
         way -> class_2561.method_43470(class_124.field_1080 + "Название: " + class_124.field_1061 + way.name())
            .method_10852(
               class_2561.method_43470(
                     class_124.field_1080
                        + " Координаты: "
                        + class_124.field_1068
                        + " ("
                        + way.pos().method_10263()
                        + ", "
                        + way.pos().method_10264()
                        + ", "
                        + way.pos().method_10260()
                        + ")"
                  )
                  .method_10852(class_2561.method_43470(class_124.field_1080 + " Сервер: " + class_124.field_1068 + way.server()))
            ),
         IBaritoneChatControl.FORCE_COMMAND_PREFIX + label
      );
   }

   private void handleClearWays(IArgConsumer args) throws CommandException {
      args.requireMax(1);
      this.wayRepository.clearList();
      this.logDirect(class_124.field_1060 + "Все метки были удалены");
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny()) {
         String arg = args.getString();
         if (!arg.equalsIgnoreCase("remove")) {
            if (arg.equalsIgnoreCase("add")) {
               String string = args.has(4) ? "" : (args.has(3) ? "z" : (args.has(2) ? "y" : (args.has(1) ? "x" : "название")));
               return new TabCompleteHelper().sortAlphabetically().prepend(string).stream();
            }

            return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(arg).stream();
         }

         if (args.hasExactlyOne()) {
            return args.tabCompleteDatatype(WayDataType.INSTANCE);
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Позволяет ставить метки в мире";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно добавлять и удалять метки в мире",
         "",
         "Использование:",
         "> way add [название] - Добавляет метку на текущем месте",
         "> way add [название] [x] [y] [z] - Добавляет метку по координатам",
         "> way remove [название] - Удаляет метку",
         "> way list - Возвращает список меток",
         "> way clear - Очищает список мето"
      );
   }
}
