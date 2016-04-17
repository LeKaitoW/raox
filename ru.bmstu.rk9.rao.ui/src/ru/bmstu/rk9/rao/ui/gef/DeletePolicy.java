package ru.bmstu.rk9.rao.ui.gef;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import ru.bmstu.rk9.rao.ui.gef.commands.CommandDelete;

public class DeletePolicy extends ComponentEditPolicy {

	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		return new CommandDelete((Node) getHost().getModel(), (Node) getHost().getParent().getModel());
	}
}
