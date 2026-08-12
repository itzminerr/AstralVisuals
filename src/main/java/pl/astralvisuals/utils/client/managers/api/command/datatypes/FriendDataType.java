package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.common.repository.friend.Friend;
import pl.astralvisuals.common.repository.friend.FriendUtils;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum FriendDataType implements IDatatypeFor<Friend> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> friends = this.getFriends().stream().map(Friend::getName);
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
   }

   public Friend get(IDatatypeContext datatypeContext) throws CommandException {
      String requestedName = datatypeContext.getConsumer().getString();
      return this.getFriends().stream().filter(s -> s.getName().equalsIgnoreCase(requestedName)).findFirst().orElse(null);
   }

   private List<? extends Friend> getFriends() {
      return FriendUtils.getFriends();
   }
}
