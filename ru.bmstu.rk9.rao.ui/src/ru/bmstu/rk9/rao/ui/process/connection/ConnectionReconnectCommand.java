package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.BlockNode;

public class ConnectionReconnectCommand extends Command {

	private Connection connection;
	private BlockNode oldSourceBlockNode;
	private BlockNode oldTargetBlockNode;
	private BlockNode newSourceBlockNode;
	private BlockNode newTargetBlockNode;
	private String oldSourceDockName;
	private String oldTargetDockName;
	private String newSourceDockName;
	private String newTargetDockName;

	public ConnectionReconnectCommand(Connection connection) {
		if (connection == null)
			throw new IllegalArgumentException();

		this.connection = connection;
		this.oldSourceBlockNode = connection.getSourceBlockNode();
		this.oldTargetBlockNode = connection.getTargetBlockNode();
		this.oldSourceDockName = connection.getSourceDockName();
		this.oldTargetDockName = connection.getTargetDockName();
	}

	@Override
	public boolean canExecute() {
		if (newSourceBlockNode != null) {
			return checkSourceReconnection();
		}
		if (newTargetBlockNode != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	private final boolean checkSourceReconnection() {
		if (newSourceBlockNode.equals(oldTargetBlockNode))
			return false;
		if (newSourceBlockNode.getDocksCount(newSourceDockName) > 0)
			return false;
		return true;
	}

	private final boolean checkTargetReconnection() {
		if (oldSourceBlockNode.equals(newTargetBlockNode))
			return false;
		return true;
	}

	public final void setNewSource(BlockNode sourceBlockNode, String sourceDockName) {
		if (sourceBlockNode == null || sourceDockName == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceBlockNode = sourceBlockNode;
		this.newTargetBlockNode = null;
		this.newSourceDockName = sourceDockName;
	}

	public final void setNewTarget(BlockNode targetBlockNode, String targetDockName) {
		if (targetBlockNode == null || targetDockName == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceBlockNode = null;
		this.newTargetBlockNode = targetBlockNode;
		this.newTargetDockName = targetDockName;
	}

	@Override
	public void execute() {
		if (newSourceBlockNode != null) {
			connection.reconnect(newSourceBlockNode, oldTargetBlockNode, newSourceDockName, oldTargetDockName);
		} else if (newTargetBlockNode != null) {
			connection.reconnect(oldSourceBlockNode, newTargetBlockNode, oldSourceDockName, newTargetDockName);
		} else {
			throw new IllegalStateException("Internal error: new source node and new target node cannot both be null");
		}
	}

	@Override
	public void undo() {
		connection.reconnect(oldSourceBlockNode, oldTargetBlockNode, oldSourceDockName, oldTargetDockName);
	}
}
