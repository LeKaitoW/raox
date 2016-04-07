package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class ConnectionReconnectCommand extends Command {

	private Connection connection;
	private NodeWithProperty oldSourceNode, oldTargetNode, newSourceNode, newTargetNode;
	private String oldSourceDockName, oldTargetDockName, newSourceDockName, newTargetDockName;

	public ConnectionReconnectCommand(Connection connection) {
		if (connection == null)
			throw new IllegalArgumentException();

		this.connection = connection;
		this.oldSourceNode = connection.getSourceNode();
		this.oldTargetNode = connection.getTargetNode();
		this.oldSourceDockName = connection.getSourceDockName();
		this.oldTargetDockName = connection.getTargetDockName();
	}

	@Override
	public boolean canExecute() {
		if (newSourceNode != null) {
			return checkSourceReconnection();
		}
		if (newTargetNode != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	private boolean checkSourceReconnection() {
		if (newSourceNode.equals(oldTargetNode))
			return false;
		if (newSourceNode.getDocksCount(newSourceDockName) > 0)
			return false;
		return true;
	}

	private boolean checkTargetReconnection() {
		if (oldSourceNode.equals(newTargetNode))
			return false;
		return true;
	}

	public void setNewSource(NodeWithProperty sourceNode, String sourceDockName) {
		if (sourceNode == null || sourceDockName == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = sourceNode;
		this.newTargetNode = null;
		this.newSourceDockName = sourceDockName;
	}

	public void setNewTarget(NodeWithProperty targetNode, String targetDockName) {
		if (targetNode == null || targetDockName == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = null;
		this.newTargetNode = targetNode;
		this.newTargetDockName = targetDockName;
	}

	@Override
	public void execute() {
		if (newSourceNode != null) {
			connection.reconnect(newSourceNode, oldTargetNode, newSourceDockName, oldTargetDockName);
		} else if (newTargetNode != null) {
			connection.reconnect(oldSourceNode, newTargetNode, oldSourceDockName, newTargetDockName);
		} else {
			throw new IllegalStateException("Internal error: new source node and new target node cannot both be null");
		}
	}

	@Override
	public void undo() {
		connection.reconnect(oldSourceNode, oldTargetNode, oldSourceDockName, oldTargetDockName);
	}
}
