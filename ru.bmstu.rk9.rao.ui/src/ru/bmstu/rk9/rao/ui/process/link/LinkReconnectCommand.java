package ru.bmstu.rk9.rao.ui.process.link;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class LinkReconnectCommand extends Command {

	private Link link;
	private NodeWithProperty oldSourceNode, oldTargetNode, newSourceNode, newTargetNode;
	private String oldSourceTerminal, oldTargetTerminal, newSourceTerminal, newTargetTerminal;

	public LinkReconnectCommand(Link link) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		this.link = link;
		this.oldSourceNode = link.getSourceNode();
		this.oldTargetNode = link.getTargetNode();
		this.oldSourceTerminal = link.getSourceDock();
		this.oldTargetTerminal = link.getTargetDock();
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
		if (newSourceNode.getDocksCount(newSourceTerminal) > 0)
			return false;
		return true;
	}

	private boolean checkTargetReconnection() {
		if (oldSourceNode.equals(newTargetNode))
			return false;
		return true;
	}

	public void setNewSource(NodeWithProperty sourceNode, String sourceTerminal) {
		if (sourceNode == null || sourceTerminal == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = sourceNode;
		this.newTargetNode = null;
		this.newSourceTerminal = sourceTerminal;
	}

	public void setNewTarget(NodeWithProperty targetNode, String targetTerminal) {
		if (targetNode == null || targetTerminal == null) {
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
