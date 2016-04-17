package ru.bmstu.rk9.rao.ui.process.commands;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;

public class DeleteCommand extends Command {

	private Node node;
	private Node parentNode;

	public DeleteCommand(Node node, Node parentNode) {
		this.node = node;
		this.parentNode = parentNode;
	}

	@Override
	public void execute() {
		parentNode.removeChild(node);
		if (node instanceof BlockNode) {
			BlockNode blockNode = (BlockNode) node;
			disconnect(blockNode.getSourceConnections());
			disconnect(blockNode.getTargetConnections());
		}
	}

	@Override
	public void undo() {
		parentNode.addChild(node);
	}

	private final void disconnect(List<Connection> connections) {
		if (connections.isEmpty())
			return;

		Iterator<Connection> iterator = connections.iterator();
		while (iterator.hasNext()) {
			Connection connection = iterator.next();
			connection.disconnect();
		}
	}
}
