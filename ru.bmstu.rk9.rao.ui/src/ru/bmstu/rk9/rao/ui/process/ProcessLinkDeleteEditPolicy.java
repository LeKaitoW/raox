package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import ru.bmstu.rk9.rao.ui.process.link.LinkDeleteCommand;

public class ProcessLinkDeleteEditPolicy extends ConnectionEditPolicy {

	@Override
	protected Command getDeleteCommand(GroupRequest arg0) {
		LinkDeleteCommand command = new LinkDeleteCommand();
		command.setLink(getHost().getModel());
		return command;
	}
}
