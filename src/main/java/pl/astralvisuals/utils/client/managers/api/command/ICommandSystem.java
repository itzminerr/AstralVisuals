package pl.astralvisuals.utils.client.managers.api.command;

import pl.astralvisuals.utils.client.managers.api.command.argparser.IArgParserManager;

public interface ICommandSystem {
   IArgParserManager getParserManager();
}
