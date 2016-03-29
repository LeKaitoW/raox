package ru.bmstu.rk9.rao.ui.process.link;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class LinkCreateCommand extends Command {

	private NodeWithProperty sourceNode, targetNode;
	private ProcessLink link;

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
		if (sourceNode.getLinksCount(sourceTerminal) > 0)
			return false;
		return true;
	}

	@Override
	public void execute() {
		link = new ProcessLink(sourceNode, targetNode, sourceTerminal, targetTerminal);
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

	public String getSourceTerminal() {
		return sourceTerminal;
	}

	public String getTargetTerminal() {
		return targetTerminal;
	}
}
