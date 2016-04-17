package ru.bmstu.rk9.rao.ui.process.commands;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class DeleteCommand extends Command {

	private Node node;
	private Node parentNode;

	@Override
	public void execute() {
		parentNode.removeChild(node);
		if (node instanceof BlockNode) {
			BlockNode blockNode = (BlockNode) node;
			disconnect(blockNode.getSourceConnections());
			disconnect(blockNode.getTargetConnections());
		}
	}

	public void setModel(Object model) {
		node = (Node) model;
	}

	public void setParentModel(Object model) {
		parentNode = (Node) model;
	}

	@Override
	public void undo() {
		parentNode.addChild(node);
	}

	private void disconnect(List<Connection> connections) {
		if (connections.isEmpty())
			return;

		Iterator<Connection> iterator = connections.iterator();
		while (iterator.hasNext()) {
			Connection connection = iterator.next();
			connection.disconnect();
		}
	}
}
