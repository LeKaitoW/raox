package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class ConnectionCreateCommand extends Command {

	private NodeWithProperty sourceNode, targetNode;
	private Connection connection;

	private String sourceTerminal;
	private String targetTerminal;

	public void setSource(NodeWithProperty sourceNode, String sourceTerminal) {
		this.sourceNode = sourceNode;
		this.sourceTerminal = sourceTerminal;
	}

	public void setTarget(NodeWithProperty targetNode, String targetTerminal) {
		this.targetNode = targetNode;
		this.targetTerminal = targetTerminal;
	}

	@Override
	public boolean canExecute() {
		if (sourceNode == null || targetNode == null)
			return false;
		if (sourceNode.equals(targetNode))
			return false;
		if (sourceNode.getDocksCount(sourceTerminal) > 0)
			return false;
		return true;
	}

	@Override
	public void execute() {
		connection = new Connection(sourceNode, targetNode, sourceTerminal, targetTerminal);
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

	public String getSourceTerminal() {
		return sourceTerminal;
	}

	public String getTargetTerminal() {
		return targetTerminal;
	}
}
