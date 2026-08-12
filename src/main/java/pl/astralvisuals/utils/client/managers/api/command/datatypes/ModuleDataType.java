package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.Force;
import pl.astralvisuals.features.module.Module;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum ModuleDataType implements IDatatypeFor<Module> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
      Stream<String> source = this.getModules().stream().map(Module::getName);
      String context = datatypeContext.getConsumer().getString();
      return new TabCompleteHelper().append(source).filterPrefix(context).sortAlphabetically().stream();
   }

   public Module get(IDatatypeContext datatypeContext) throws CommandException {
      String name = datatypeContext.getConsumer().getString();
      return this.getModules().stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   private List<? extends Module> getModules() {
      return Force.getInstance().getModuleRepository().modules();
   }
}
