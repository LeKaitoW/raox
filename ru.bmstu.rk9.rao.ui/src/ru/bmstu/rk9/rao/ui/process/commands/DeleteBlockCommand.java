package ru.bmstu.rk9.rao.ui.process.commands;

import java.util.Iterator;
import java.util.List;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.commands.CommandDelete;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;

public class DeleteBlockCommand extends CommandDelete {

	public DeleteBlockCommand(Node node, Node parentNode) {
		super(node, parentNode);
	}

	@Override
	public void execute() {
		super.execute();
		if (node instanceof BlockNode) {
			BlockNode blockNode = (BlockNode) node;
			disconnect(blockNode.getSourceConnections());
			disconnect(blockNode.getTargetConnections());
		}
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
