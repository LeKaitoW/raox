package ru.bmstu.rk9.rao.ui.process.link;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class Link implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static String name = "ProcessLink";
	protected NodeWithProperty sourceNode;
	protected NodeWithProperty targetNode;

	private String sourceTerminal;
	private String targetTerminal;

	public Link(NodeWithProperty sourceNode, NodeWithProperty targetNode, String sourceTerminal,
			String targetTerminal) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.setSourceTerminal(sourceTerminal);
		this.setTargetTerminal(targetTerminal);
	}

	public NodeWithProperty getSourceNode() {
		return sourceNode;
	}

	public NodeWithProperty getTargetNode() {
		return targetNode;
	}

	public void connect() {
		sourceNode.addLink(this);
		targetNode.addLink(this);
		sourceNode.increaseLinksCount(sourceTerminal);
		targetNode.increaseLinksCount(targetTerminal);
	}

	public void disconnect() {
		sourceNode.removeLink(this);
		targetNode.removeLink(this);
		sourceNode.decreaseLinksCount(sourceTerminal);
		targetNode.decreaseLinksCount(targetTerminal);
	}

	public void reconnect(NodeWithProperty sourceNode, NodeWithProperty targetNode, String sourceTerminal,
			String targetTerminal) {
		if (sourceNode == null || targetNode == null || sourceNode == targetNode) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.sourceTerminal = sourceTerminal;
		this.targetTerminal = targetTerminal;
		connect();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getSourceTerminal() {
		return sourceTerminal;
	}

	public void setSourceTerminal(String sourceTerminal) {
		this.sourceTerminal = sourceTerminal;
	}

	public String getTargetTerminal() {
		return targetTerminal;
	}

	public void setTargetTerminal(String targetTerminal) {
		this.targetTerminal = targetTerminal;
	}
}
