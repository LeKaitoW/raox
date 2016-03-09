package ru.bmstu.rk9.rao.ui.process.link;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class LinkReconnectCommand extends Command {

	private ProcessLink link;
	private NodeWithProperty oldSourceNode, oldTargetNode, newSourceNode, newTargetNode;
	private String oldSourceTerminal, oldTargetTerminal, newSourceTerminal, newTargetTerminal;

	public LinkReconnectCommand(ProcessLink link) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		this.link = link;
		this.oldSourceNode = link.getSourceNode();
		this.oldTargetNode = link.getTargetNode();
		this.oldSourceTerminal = link.getSourceTerminal();
		this.oldTargetTerminal = link.getTargetTerminal();
	}

	@Override
	public boolean canExecute() {
		if (newSourceNode != null) {
			return checkSourceReconnection();
		} else if (newTargetNode != null) {
			return checkTargetReconnection();
		}
		return false;
	}

	private boolean checkSourceReconnection() {
		if (newSourceNode == null)
			return false;
		if (newSourceNode.equals(oldTargetNode))
			return false;
		return true;
	}

	private boolean checkTargetReconnection() {
		if (newTargetNode == null)
			return false;
		if (oldSourceNode.equals(newTargetNode))
			return false;
		return true;
	}

	public void setNewSource(NodeWithProperty sourceNode, String sourceTerminal) {
		if (sourceNode == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = sourceNode;
		this.newTargetNode = null;
		this.newSourceTerminal = sourceTerminal;
	}

	public void setNewTarget(NodeWithProperty targetNode, String targetTerminal) {
		if (targetNode == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = null;
		this.newTargetNode = targetNode;
		this.newTargetTerminal = targetTerminal;
	}

	@Override
	public void execute() {
		if (newSourceNode != null) {
			link.reconnect(newSourceNode, oldTargetNode, newSourceTerminal, oldTargetTerminal);
		} else if (newTargetNode != null) {
			link.reconnect(oldSourceNode, newTargetNode, oldSourceTerminal, newTargetTerminal);
		} else {
			throw new IllegalStateException("Internal error: new source node and new target node cannot both be null");
		}
	}

	@Override
	public void undo() {
		link.reconnect(oldSourceNode, oldTargetNode, oldSourceTerminal, oldTargetTerminal);
	}
}
