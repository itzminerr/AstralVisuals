package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import pl.astralvisuals.Force;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.client.managers.api.command.helpers.TabCompleteHelper;

public enum ConfigFileDataType implements IDatatypeFor<String> {
   INSTANCE;

   @Override
   public Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException {
      Stream<String> friends = this.getConfigs().stream().map(String::toString);
      String context = ctx.getConsumer().getString();
      return new TabCompleteHelper().append(friends).filterPrefix(context).sortAlphabetically().stream();
   }

   public String get(IDatatypeContext datatypeContext) throws CommandException {
      String requestedName = datatypeContext.getConsumer().getString();
      return this.getConfigs().stream().filter(s -> s.equalsIgnoreCase(requestedName)).findFirst().orElse(null);
   }

   public List<String> getConfigs() {
      List<String> configs = new ArrayList<>();
      File[] configFiles = Force.getInstance().getClientInfoProvider().configsDir().listFiles();
      if (configFiles != null) {
         for (File configFile : configFiles) {
            if (configFile.isFile() && configFile.getName().endsWith(".json")) {
               String configName = configFile.getName().replace(".json", "");
               configs.add(configName);
            }
         }
      }

      return configs;
   }
}
