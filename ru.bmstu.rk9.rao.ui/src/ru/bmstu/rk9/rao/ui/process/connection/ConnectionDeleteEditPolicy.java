package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class ConnectionDeleteEditPolicy extends ConnectionEditPolicy {

	@Override
	protected Command getDeleteCommand(GroupRequest arg0) {
		ConnectionDeleteCommand command = new ConnectionDeleteCommand();
		command.setConnection(getHost().getModel());
		return command;
	}
}
