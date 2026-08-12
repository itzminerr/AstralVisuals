package pl.astralvisuals.commands.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import pl.astralvisuals.common.repository.friend.FriendUtils;
import pl.astralvisuals.utils.client.managers.api.command.Command;
import pl.astralvisuals.utils.client.managers.api.command.IBaritoneChatControl;
import pl.astralvisuals.utils.client.managers.api.command.argument.IArgConsumer;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.FriendDataType;
import pl.astralvisuals.utils.client.managers.api.command.datatypes.TabPlayerDataType;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.Paginator;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public class FriendCommand extends Command {
   protected FriendCommand() {
      super("friend");
   }

   @Override
   public void execute(String label, IArgConsumer args) throws CommandException {
      String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
      args.requireMax(1);
      if (arg.contains("add")) {
         String name = args.getString();
         if (!FriendUtils.isFriend(name)) {
            FriendUtils.addFriend(name);
            this.logDirect("Вы успешно добавили " + name + " в список друзей");
         } else {
            this.logDirect(name + " уже есть в списке друзей", class_124.field_1061);
         }
      } else if (arg.contains("remove")) {
         String name = args.getString();
         if (FriendUtils.isFriend(name)) {
            FriendUtils.removeFriend(name);
            this.logDirect("Вы успешно удалили " + name + " из списка друзей");
         } else {
            this.logDirect(name + " не найден в списке друзей", class_124.field_1061);
         }
      } else if (arg.contains("clear")) {
         FriendUtils.clear();
         this.logDirect("Список друзей очищен");
      } else {
         Paginator.paginate(args, new Paginator<>(FriendUtils.getFriends()), () -> this.logDirect("Список друзей:"), friend -> {
            class_5250 namesComponent = class_2561.method_43470(friend.getName());
            namesComponent.method_10862(namesComponent.method_10866().method_10977(class_124.field_1068));
            return namesComponent;
         }, IBaritoneChatControl.FORCE_COMMAND_PREFIX + label);
      }
   }

   @Override
   public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
      if (args.hasAny()) {
         String arg = args.getString();
         if (!args.hasExactlyOne()) {
            return new TabCompleteHelper().sortAlphabetically().prepend("add", "remove", "list", "clear").filterPrefix(arg).stream();
         }

         if (arg.equalsIgnoreCase("add")) {
            return args.tabCompleteDatatype(TabPlayerDataType.INSTANCE);
         }

         if (arg.equalsIgnoreCase("remove")) {
            return args.tabCompleteDatatype(FriendDataType.INSTANCE);
         }
      }

      return Stream.empty();
   }

   @Override
   public String getShortDesc() {
      return "Позволяет управлять списком друзей";
   }

   @Override
   public List<String> getLongDesc() {
      return Arrays.asList(
         "С помощью этой команды можно добавлять и удалять друзей в клиенте",
         "",
         "Использование:",
         "> friend add [игрок] - Добавляет имя в список друзей",
         "> friend remove [игрок] - Удаляет имя из списка друзей",
         "> friend list - Показывает список друзей",
         "> friend clear - Очищает список друзей"
      );
   }
}
