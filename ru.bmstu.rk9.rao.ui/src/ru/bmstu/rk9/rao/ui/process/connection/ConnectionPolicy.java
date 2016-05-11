package ru.bmstu.rk9.rao.ui.process.connection;

import java.util.List;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.process.blocks.generate.GenerateNode;
import ru.bmstu.rk9.rao.ui.process.blocks.seize.SeizeNode;

public class ConnectionPolicy extends GraphicalNodeEditPolicy {

	@Override
	public void showSourceFeedback(Request request) {
		// System.out.println("showSourceFeedback, request = " +
		// request.toString());

		if (request instanceof ChangeBoundsRequest) {
			ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
			List<?> editParts = changeBoundsRequest.getEditParts();
			if (editParts.size() == 1) {
				BlockEditPart blockEditPart = (BlockEditPart) editParts.get(0);
				BlockNode blockNode = (BlockNode) blockEditPart.getModel();
				Node modelNode = blockNode.getRoot();
				BlockNode node1 = (BlockNode) modelNode.getChildren().get(0);
				BlockNode node2 = (BlockNode) modelNode.getChildren().get(2);
				Connection connection = new Connection(node1, node2, GenerateNode.DOCK_OUT, SeizeNode.DOCK_IN);
				connection.connect();
			}
		}

		super.showSourceFeedback(request);
	}

	@Override
	public void showTargetFeedback(Request request) {
		// System.out.println("showTargetFeedback, request = " +
		// request.toString());
		super.showTargetFeedback(request);
	}

	@Override
	protected void showTargetConnectionFeedback(DropRequest request) {
		// System.out.println("showTargetConnectionFeedback, request = " +
		// request.toString());
		super.showTargetConnectionFeedback(request);
	}

	@Override
	protected void showCreationFeedback(CreateConnectionRequest request) {
		// System.out.println("showCreationFeedback, request = " +
		// request.toString());
		super.showCreationFeedback(request);
	}

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
		ConnectionAnchor connectionAnchor = getBlockEditPart().getSourceConnectionAnchor(request);
		String sourceDockName = getBlockEditPart().mapConnectionAnchorToDock(connectionAnchor);
		if (sourceDockName == null)
			return null;
		request.setStartCommand(command);

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

	private final BlockEditPart getBlockEditPart() {
		return (BlockEditPart) getHost();
	}

	private final BlockNode getBlockNode() {
		return (BlockNode) getHost().getModel();
	}
}
