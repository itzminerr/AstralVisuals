package pl.astralvisuals.utils.client.managers.api.command.datatypes;

import pl.astralvisuals.utils.client.managers.api.command.exception.CommandException;

public interface IDatatypeFor<T> extends IDatatype {
   T get(IDatatypeContext var1) throws CommandException;
}
