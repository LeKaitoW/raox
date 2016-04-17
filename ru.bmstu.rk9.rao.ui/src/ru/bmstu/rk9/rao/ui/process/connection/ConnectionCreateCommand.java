package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.BlockNode;

public class ConnectionCreateCommand extends Command {

	private Connection connection;
	private BlockNode sourceBlockNode;
	private BlockNode targetBlockNode;
	private String sourceDockName;
	private String targetDockName;

	protected final void setSource(BlockNode sourceBlockNode, String sourceDockName) {
		this.sourceBlockNode = sourceBlockNode;
		this.sourceDockName = sourceDockName;
	}

	protected final void setTarget(BlockNode targetBlockNode, String targetDockName) {
		this.targetBlockNode = targetBlockNode;
		this.targetDockName = targetDockName;
	}

	@Override
	public boolean canExecute() {
		if (sourceBlockNode == null || targetBlockNode == null)
			return false;
		if (sourceBlockNode.equals(targetBlockNode))
			return false;
		if (sourceBlockNode.getDocksCount(sourceDockName) > 0)
			return false;
		return true;
	}

	@Override
	public void execute() {
		connection = new Connection(sourceBlockNode, targetBlockNode, sourceDockName, targetDockName);
		connection.connect();
	}

	@Override
	public boolean canUndo() {
		if (sourceBlockNode == null || targetBlockNode == null || connection == null)
			return false;
		return true;
	}

	@Override
	public void undo() {
		connection.disconnect();
	}
}
