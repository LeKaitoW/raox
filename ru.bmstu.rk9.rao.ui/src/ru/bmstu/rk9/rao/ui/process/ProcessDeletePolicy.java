package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.commands.DeleteBlockCommand;

public class ProcessDeletePolicy extends ComponentEditPolicy {

	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		return new DeleteBlockCommand((Node) getHost().getModel(), (Node) getHost().getParent().getModel());
	}
}
