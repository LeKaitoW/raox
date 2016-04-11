package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionCreateCommand;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionReconnectCommand;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;

public class ConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = (ConnectionCreateCommand) request.getStartCommand();
		BlockNode targetBlockNode = getBlockNode();
		ConnectionAnchor connectionAnchor = getProcessEditPart().getTargetConnectionAnchor(request);
		String targetDockName = getProcessEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setTarget(targetBlockNode, targetDockName);
		return command;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = new ConnectionCreateCommand();
		BlockNode sourceBlockNode = getBlockNode();
		request.setStartCommand(command);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceDockName = getProcessEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setSource(sourceBlockNode, sourceDockName);
		return command;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		BlockNode sourceBlockNode = getBlockNode();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceDockName = getProcessEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setNewSource(sourceBlockNode, sourceDockName);
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		BlockNode targetBlockNode = getBlockNode();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getTargetConnectionAnchor(request);
		String targetDockName = getProcessEditPart().mapConnectionAnchorToDock(connectionAnchor);
		command.setNewTarget(targetBlockNode, targetDockName);
		return command;
	}

	protected ProcessEditPart getProcessEditPart() {
		return (ProcessEditPart) getHost();
	}

	protected BlockNode getBlockNode() {
		return (BlockNode) getHost().getModel();
	}
}
