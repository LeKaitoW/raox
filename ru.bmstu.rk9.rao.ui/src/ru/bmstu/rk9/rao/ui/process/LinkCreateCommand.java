package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;

public class LinkCreateCommand extends Command {

	private NodeWithProperty sourceNode, targetNode;
	private ProcessLink link;

	public void setSourceNode(NodeWithProperty sourceNode) {
		this.sourceNode = sourceNode;
	}

	public void setTargetNode(NodeWithProperty targetNode) {
		this.targetNode = targetNode;
	}

	@Override
	public boolean canExecute() {
		if (sourceNode == null || targetNode == null)
			return false;
		else if (sourceNode.equals(targetNode))
			return false;
		return true;
	}

	@Override
	public void execute() {
		link = new ProcessLink(sourceNode, targetNode);
		link.connect();
	}

	@Override
	public boolean canUndo() {
		if (sourceNode == null || targetNode == null || link == null)
			return false;
		return true;
	}

	@Override
	public void undo() {
		link.disconnect();
	}
}
