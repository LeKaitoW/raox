package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionCreateCommand;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionReconnectCommand;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class ConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = (ConnectionCreateCommand) request.getStartCommand();
		NodeWithProperty targetNode = getNodeWithProperty();
		ConnectionAnchor connectionAnchor = getProcessEditPart().getTargetConnectionAnchor(request);
		String targetTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setTarget(targetNode, targetTerminal);
		return command;
	}

	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		ConnectionCreateCommand command = new ConnectionCreateCommand();
		NodeWithProperty sourceNode = getNodeWithProperty();
		request.setStartCommand(command);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setSource(sourceNode, sourceTerminal);
		return command;
	}

	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		NodeWithProperty sourceNode = getNodeWithProperty();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
		ConnectionAnchor connectionAnchor = getProcessEditPart().getSourceConnectionAnchor(request);
		String sourceTerminal = getProcessEditPart().mapConnectionAnchorToTerminal(connectionAnchor);
		command.setNewSource(sourceNode, sourceTerminal);
		return command;
	}

	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		Connection connection = (Connection) request.getConnectionEditPart().getModel();
		NodeWithProperty targetNode = getNodeWithProperty();
		ConnectionReconnectCommand command = new ConnectionReconnectCommand(connection);
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
