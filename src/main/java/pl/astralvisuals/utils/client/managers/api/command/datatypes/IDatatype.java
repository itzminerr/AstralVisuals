package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import java.util.stream.Stream;
import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;
import pl.astralvisuals.utils.display.interfaces.QuickImports;

public interface IDatatype extends QuickImports {
   Stream<String> tabComplete(IDatatypeContext var1) throws CommandException;
}
