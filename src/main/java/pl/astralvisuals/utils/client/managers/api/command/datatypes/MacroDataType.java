package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.Force;
import pl.astralvisuals.common.repository.macro.Macro;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum MacroDataType implements IDatatypeFor<Macro> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> macros = this.getMacro().stream().map(Macro::name);
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(macros).filterPrefix(context).sortAlphabetically().stream();
   }

   public Macro get(IDatatypeContext datatypeContext) throws CommandException {
      String requestedName = datatypeContext.getConsumer().getString();
      return this.getMacro().stream().filter(s -> s.name().equalsIgnoreCase(requestedName)).findFirst().orElse(null);
   }

   private List<? extends Macro> getMacro() {
      return Force.getInstance().getMacroRepository().macroList;
   }
}
