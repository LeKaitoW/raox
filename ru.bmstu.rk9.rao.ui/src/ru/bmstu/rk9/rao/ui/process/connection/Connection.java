package ru.bmstu.rk9.rao.ui.process.connection;

import java.io.Serializable;

import org.eclipse.core.runtime.IAdaptable;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;

public class Connection implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static String name = "Connection";
	protected BlockNode sourceBlockNode;
	protected BlockNode targetBlockNode;

	private String sourceDockName;
	private String targetDockName;

	public Connection(BlockNode sourceBlockNode, BlockNode targetBlockNode, String sourceDockName,
			String targetDockName) {
		this.sourceBlockNode = sourceBlockNode;
		this.targetBlockNode = targetBlockNode;
		setSourceDockName(sourceDockName);
		setTargetDockName(targetDockName);
	}

	public BlockNode getSourceBlockNode() {
		return sourceBlockNode;
	}

	public BlockNode getTargetBlockNode() {
		return targetBlockNode;
	}

	public void connect() {
		sourceBlockNode.addConnection(this);
		targetBlockNode.addConnection(this);
		sourceBlockNode.captureDock(sourceDockName);
		targetBlockNode.captureDock(targetDockName);
	}

	public void disconnect() {
		sourceBlockNode.removeConnection(this);
		targetBlockNode.removeConnection(this);
		sourceBlockNode.releaseDock(sourceDockName);
		targetBlockNode.releaseDock(targetDockName);
	}

	public void reconnect(BlockNode sourceBlockNode, BlockNode targetBlockNode, String sourceDockName,
			String targetDockName) {
		if (sourceBlockNode == null || targetBlockNode == null || sourceBlockNode == targetBlockNode) {
			throw new IllegalArgumentException();
		}
		disconnect();
		this.sourceBlockNode = sourceBlockNode;
		this.targetBlockNode = targetBlockNode;
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
