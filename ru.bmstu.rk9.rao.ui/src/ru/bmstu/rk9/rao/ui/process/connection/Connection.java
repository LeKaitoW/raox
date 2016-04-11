package ru.bmstu.rk9.rao.ui.process.connection;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

import ru.bmstu.rk9.rao.ui.process.node.NodeWithConnections;

public class Connection implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static String name = "Connection";
	protected NodeWithConnections sourceNode;
	protected NodeWithConnections targetNode;

	private String sourceDockName;
	private String targetDockName;

	public Connection(NodeWithConnections sourceNode, NodeWithConnections targetNode, String sourceDockName,
			String targetDockName) {
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		setSourceDockName(sourceDockName);
		setTargetDockName(targetDockName);
	}

	public NodeWithConnections getSourceNode() {
		return sourceNode;
	}

	public NodeWithConnections getTargetNode() {
		return targetNode;
	}

	public void connect() {
		sourceNode.addConnection(this);
		targetNode.addConnection(this);
		sourceNode.captureDock(sourceDockName);
		targetNode.captureDock(targetDockName);
	}

	public void disconnect() {
		sourceNode.removeConnection(this);
		targetNode.removeConnection(this);
		sourceNode.releaseDock(sourceDockName);
		targetNode.releaseDock(targetDockName);
	}

	public void reconnect(NodeWithConnections sourceNode, NodeWithConnections targetNode, String sourceDockName,
			String targetDockName) {
		if (sourceNode == null || targetNode == null || sourceNode == targetNode) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.sourceDockName = sourceDockName;
		this.targetDockName = targetDockName;
		connect();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getSourceDockName() {
		return sourceDockName;
	}

	public void setSourceDockName(String sourceDockName) {
		this.sourceDockName = sourceDockName;
	}

	public String getTargetDockName() {
		return targetDockName;
	}

	public void setTargetDockName(String targetDockName) {
		this.targetDockName = targetDockName;
	}
}
