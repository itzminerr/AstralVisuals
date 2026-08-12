package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.way.Way;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum WayDataType implements IDatatypeFor<Way> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> ways = this.getWay().stream().map(Way::name);
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(ways).filterPrefix(context).sortAlphabetically().stream();
   }

   public Way get(IDatatypeContext datatypeContext) throws CommandException {
      String text = datatypeContext.getConsumer().getString();
      return this.getWay().stream().filter(s -> s.name().equalsIgnoreCase(text)).findFirst().orElse(null);
   }

   private List<? extends Way> getWay() {
      return Force.getInstance().getWayRepository().wayList;
   }
}
