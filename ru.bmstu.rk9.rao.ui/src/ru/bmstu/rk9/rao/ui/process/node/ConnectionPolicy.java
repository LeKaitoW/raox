package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionCreateCommand;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionReconnectCommand;

public class ConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = (ConnectionCreateCommand) request.getStartCommand();
		BlockNode targetBlockNode = getBlockNode();
		ConnectionAnchor connectionAnchor = getBlockEditPart().getTargetConnectionAnchor(request);
		String targetDockName = getBlockEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setTarget(targetBlockNode, targetDockName);
		return command;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = new ConnectionCreateCommand();
		BlockNode sourceBlockNode = getBlockNode();
		request.setStartCommand(command);
		ConnectionAnchor connectionAnchor = getBlockEditPart().getSourceConnectionAnchor(request);
		String sourceDockName = getBlockEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setSource(sourceBlockNode, sourceDockName);
		return command;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		BlockNode sourceBlockNode = getBlockNode();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
		ConnectionAnchor connectionAnchor = getBlockEditPart().getSourceConnectionAnchor(request);
		String sourceDockName = getBlockEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setNewSource(sourceBlockNode, sourceDockName);
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		BlockNode targetBlockNode = getBlockNode();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
		ConnectionAnchor connectionAnchor = getBlockEditPart().getTargetConnectionAnchor(request);
		String targetDockName = getBlockEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setNewTarget(targetBlockNode, targetDockName);
		return command;
	}

	protected BlockEditPart getBlockEditPart() {
		return (BlockEditPart) getHost();
	}

	protected BlockNode getBlockNode() {
		return (BlockNode) getHost().getModel();
	}
}
