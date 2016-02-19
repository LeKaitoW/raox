package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.commands.Command;

public class LinkReconnectCommand extends Command {

	private ProcessLink link;
	private NodeWithProperty oldSourceNode, oldTargetNode, newSourceNode, newTargetNode;

	public LinkReconnectCommand(ProcessLink link) {
		if (link == null) {
			throw new IllegalArgumentException();
		}
		this.link = link;
		this.oldSourceNode = link.getSourceNode();
		this.oldTargetNode = link.getTargetNode();
	}

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
		else if (newSourceNode.equals(oldTargetNode))
			return false;
		else if (!newSourceNode.getClass().equals(oldTargetNode.getClass()))
			return false;
		return true;
	}

	private boolean checkTargetReconnection() {
		if (newTargetNode == null)
			return false;
		else if (oldSourceNode.equals(newTargetNode))
			return false;
		else if (!oldSourceNode.getClass().equals(newTargetNode.getClass()))
			return false;
		return true;
	}

	public void setNewSourceNode(NodeWithProperty sourceNode) {
		if (sourceNode == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = sourceNode;
		this.newTargetNode = null;
	}

	public void setNewTargetNode(NodeWithProperty targetNode) {
		if (targetNode == null) {
			throw new IllegalArgumentException();
		}
		this.newSourceNode = null;
		this.newTargetNode = targetNode;
	}

	public void execute() {
		if (newSourceNode != null) {
			link.reconnect(newSourceNode, oldTargetNode);
		} else if (newTargetNode != null) {
			link.reconnect(oldSourceNode, newTargetNode);
		} else {
			throw new IllegalStateException("Should not happen");
		}
	}

	public void undo() {
		link.reconnect(oldSourceNode, oldTargetNode);
	}

}
