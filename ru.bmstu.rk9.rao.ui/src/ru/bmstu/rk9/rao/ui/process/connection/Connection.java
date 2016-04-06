package ru.bmstu.rk9.rao.ui.process.connection;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class Connection implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static String name = "Connection";
	protected NodeWithProperty sourceNode;
	protected NodeWithProperty targetNode;

	private String sourceDock;
	private String targetDock;

	public Connection(NodeWithProperty sourceNode, NodeWithProperty targetNode, String sourceDock, String targetDock) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		setSourceDock(sourceDock);
		setTargetDock(targetDock);
	}

	public NodeWithProperty getSourceNode() {
		return sourceNode;
	}

	public NodeWithProperty getTargetNode() {
		return targetNode;
	}

	public void connect() {
		sourceNode.addConnection(this);
		targetNode.addConnection(this);
		sourceNode.captureDock(sourceDock);
		targetNode.captureDock(targetDock);
	}

	public void disconnect() {
		sourceNode.removeConnection(this);
		targetNode.removeConnection(this);
		sourceNode.releaseDock(sourceDock);
		targetNode.releaseDock(targetDock);
	}

	public void reconnect(NodeWithProperty sourceNode, NodeWithProperty targetNode, String sourceDock,
			String targetDock) {
		if (sourceNode == null || targetNode == null || sourceNode == targetNode) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.sourceDock = sourceDock;
		this.targetDock = targetDock;
		connect();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getSourceDock() {
		return sourceDock;
	}

	public void setSourceDock(String sourceDock) {
		this.sourceDock = sourceDock;
	}

	public String getTargetDock() {
		return targetDock;
	}

	public void setTargetDock(String targetDock) {
		this.targetDock = targetDock;
	}
}
