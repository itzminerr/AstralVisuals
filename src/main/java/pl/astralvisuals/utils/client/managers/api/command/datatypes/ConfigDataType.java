package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;
import pl.astralvisuals.utils.client.managers.file.ClientFile;
import pl.astralvisuals.utils.client.managers.file.impl.ModuleFile;

public enum ConfigDataType implements IDatatypeFor<ClientFile> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
      Stream<String> friends = this.getList().stream().map(ClientFile::getName);
      String context = ctx.getConsumer().getString();
      return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
   }

   public ClientFile get(IDatatypeContext datatypeContext) throws CommandException {
      String requestedName = datatypeContext.getConsumer().getString();
      return this.getList().stream().filter(s -> s.getName().equalsIgnoreCase(requestedName)).findFirst().orElse(null);
   }

   public List<? extends ModuleFile> getList() {
      return Force.getInstance()
         .getFileRepository()
         .getClientFiles()
         .stream()
         .filter(clientFile -> clientFile instanceof ModuleFile)
         .map(clientFile -> (ModuleFile)clientFile)
         .collect(Collectors.toList());
   }
}
