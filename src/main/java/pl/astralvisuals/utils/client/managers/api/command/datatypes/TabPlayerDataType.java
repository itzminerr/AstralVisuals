package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.class_268;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum TabPlayerDataType implements IDatatypeFor<class_268> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
      return new TabCompleteHelper()
         .append(this.getTeam().stream().<Collection>map(class_268::method_1204).map(Object::toString).map(s -> s.replaceAll("[\\[\\]]", "")))
         .filterPrefix(ctx.getConsumer().getString())
         .sortAlphabetically()
         .stream();
   }

   public class_268 get(IDatatypeContext datatypeContext) throws CommandException {
      String requestedName = datatypeContext.getConsumer().getString();
      return this.getTeam().stream().filter(s -> s.method_1197().equalsIgnoreCase(requestedName)).findFirst().orElse(null);
   }

   public Collection<class_268> getTeam() {
      return mc.field_1687.method_8428().method_1159();
   }
}
