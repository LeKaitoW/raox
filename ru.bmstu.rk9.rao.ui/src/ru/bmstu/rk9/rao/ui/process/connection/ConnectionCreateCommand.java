package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class ConnectionCreateCommand extends Command {

	private NodeWithProperty sourceNode, targetNode;
	private Connection connection;

	private String sourceDock;
	private String targetDock;

	public void setSource(NodeWithProperty sourceNode, String sourceDock) {
		this.sourceNode = sourceNode;
		this.sourceDock = sourceDock;
	}

	public void setTarget(NodeWithProperty targetNode, String targetDock) {
		this.targetNode = targetNode;
		this.targetDock = targetDock;
	}

	@Override
	public boolean canExecute() {
		if (sourceNode == null || targetNode == null)
			return false;
		if (sourceNode.equals(targetNode))
			return false;
		if (sourceNode.getDocksCount(sourceDock) > 0)
			return false;
		return true;
	}

	@Override
	public void execute() {
		connection = new Connection(sourceNode, targetNode, sourceDock, targetDock);
		connection.connect();
	}

	@Override
	public boolean canUndo() {
		if (sourceNode == null || targetNode == null || connection == null)
			return false;
		return true;
	}

	@Override
	public void undo() {
		connection.disconnect();
	}

	public final String getSourceDock() {
		return sourceDock;
	}

	public final String getTargetDock() {
		return targetDock;
	}
}
