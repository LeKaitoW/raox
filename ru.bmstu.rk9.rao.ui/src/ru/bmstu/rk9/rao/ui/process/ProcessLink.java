package ru.bmstu.rk9.rao.ui.process;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

public class ProcessLink implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static String name = "ProcessLink";
	protected NodeWithProperty sourceNode;
	protected NodeWithProperty targetNode;

	public ProcessLink(NodeWithProperty sourceNode, NodeWithProperty targetNode) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
	}

	public NodeWithProperty getSourceNode() {
		return sourceNode;
	}

	public NodeWithProperty getTargetNode() {
		return targetNode;
	}

	public void connect() {
		sourceNode.addConnections(this);
		targetNode.addConnections(this);
	}

	public void disconnect() {
		sourceNode.removeConnection(this);
		targetNode.removeConnection(this);
	}

	public void reconnect(NodeWithProperty sourceNode, NodeWithProperty targetNode) {
		if (sourceNode == null || targetNode == null || sourceNode == targetNode) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		connect();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}
}
