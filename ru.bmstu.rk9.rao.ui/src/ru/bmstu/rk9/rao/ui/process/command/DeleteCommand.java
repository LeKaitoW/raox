package ru.bmstu.rk9.rao.ui.process.command;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.Node;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithConnections;

public class DeleteCommand extends Command {

	private NodeWithConnections node;
	private Node parentModel;

	@Override
	public void execute() {
		this.parentModel.removeChild(node);
		disconnect(node.getSourceConnections());
		disconnect(node.getTargetConnections());
	}

	public void setModel(Object model) {
		this.node = (NodeWithConnections) model;
	}

	public void setParentModel(Object model) {
		parentModel = (Node) model;
	}

	@Override
	public void undo() {
		this.parentModel.addChild(node);
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
