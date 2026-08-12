package pl.astralvisuals.commands;

import pl.astralvisuals.commands.argparser.ArgParserManager;
import pl.astralvisuals.utils.client.managers.api.command.ICommandSystem;
import pl.astralvisuals.utils.client.managers.api.command.argparser.IArgParserManager;

public enum CommandSystem implements ICommandSystem {
   INSTANCE;

   @Override
   public IArgParserManager getParserManager() {
      return ArgParserManager.INSTANCE;
   }
}
