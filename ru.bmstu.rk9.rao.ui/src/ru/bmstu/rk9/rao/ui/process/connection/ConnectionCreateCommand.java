package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class ConnectionCreateCommand extends Command {

	private NodeWithProperty sourceNode, targetNode;
	private Connection connection;

	private String sourceDockName;
	private String targetDockName;

	public void setSource(NodeWithProperty sourceNode, String sourceDockName) {
		this.sourceNode = sourceNode;
		this.sourceDockName = sourceDockName;
	}

	public void setTarget(NodeWithProperty targetNode, String targetDockName) {
		this.targetNode = targetNode;
		this.targetDockName = targetDockName;
	}

	@Override
	public boolean canExecute() {
		if (sourceNode == null || targetNode == null)
			return false;
		if (sourceNode.equals(targetNode))
			return false;
		if (sourceNode.getDocksCount(sourceDockName) > 0)
			return false;
		return true;
	}

	@Override
	public void execute() {
		connection = new Connection(sourceNode, targetNode, sourceDockName, targetDockName);
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

	public final String getSourceDockName() {
		return sourceDockName;
	}

	public final String getTargetDockName() {
		return targetDockName;
	}
}
