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
		NodeWithProperty targetNode = getNodeWithProperty();
		ConnectionAnchor connectionAnchor = getProcessEditPart().getTargetConnectionAnchor(request);
		String targetTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setTarget(targetNode, targetTerminal);
		return command;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		LinkCreateCommand command = new LinkCreateCommand();
		NodeWithProperty sourceNode = getNodeWithProperty();
		request.setStartCommand(command);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setSource(sourceNode, sourceTerminal);
		return command;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		ProcessLink link = (ProcessLink) request.getConnectionEditPart().getModel();
		NodeWithProperty sourceNode = getNodeWithProperty();
		LinkReconnectCommand command = new LinkReconnectCommand(link);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setNewSource(sourceNode, sourceTerminal);
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		ProcessLink link = (ProcessLink) request.getConnectionEditPart().getModel();
		NodeWithProperty targetNode = getNodeWithProperty();
		LinkReconnectCommand command = new LinkReconnectCommand(link);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getTargetConnectionAnchor(request);
		String targetTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setNewTarget(targetNode, targetTerminal);
		return command;
	}

	protected ProcessEditPart getProcessEditPart() {
		return (ProcessEditPart) getHost();
	}

	protected NodeWithProperty getNodeWithProperty() {
		return (NodeWithProperty) getHost().getModel();
	}
}
