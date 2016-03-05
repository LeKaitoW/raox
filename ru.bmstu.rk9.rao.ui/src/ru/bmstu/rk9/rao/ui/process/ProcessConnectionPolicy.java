package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import ru.bmstu.rk9.rao.ui.process.link.LinkCreateCommand;
import ru.bmstu.rk9.rao.ui.process.link.LinkReconnectCommand;
import ru.bmstu.rk9.rao.ui.process.link.ProcessLink;

public class ProcessConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		LinkCreateCommand command = (LinkCreateCommand) request.getStartCommand();
		NodeWithProperty targetNode = (NodeWithProperty) getHost().getModel();
		command.setTargetNode(targetNode);
		ConnectionAnchor connectionAnchor = ((ProcessEditPart) getHost()).getTargetConnectionAnchor(request);
		String targetTerminal = ((ProcessEditPart) getHost()).mapConnectionAnchorToTerminal(connectionAnchor);
		command.setTargetTerminal(targetTerminal);
		return command;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		LinkCreateCommand command = new LinkCreateCommand();
		NodeWithProperty sourceNode = (NodeWithProperty) getHost().getModel();
		command.setSourceNode(sourceNode);
		request.setStartCommand(command);
		ConnectionAnchor connectionAnchor = ((ProcessEditPart) getHost()).getSourceConnectionAnchor(request);
		String sourceTerminal = ((ProcessEditPart) getHost()).mapConnectionAnchorToTerminal(connectionAnchor);
		command.setSourceTerminal(sourceTerminal);
		return command;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		ProcessLink link = (ProcessLink) request.getConnectionEditPart().getModel();
		NodeWithProperty sourceNode = (NodeWithProperty) getHost().getModel();
		LinkReconnectCommand command = new LinkReconnectCommand(link);
		command.setNewSourceNode(sourceNode);
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		ProcessLink link = (ProcessLink) request.getConnectionEditPart().getModel();
		NodeWithProperty targetNode = (NodeWithProperty) getHost().getModel();
		LinkReconnectCommand command = new LinkReconnectCommand(link);
		command.setNewTargetNode(targetNode);
		return command;
	}

}
