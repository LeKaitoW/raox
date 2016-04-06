package ru.bmstu.rk9.rao.ui.process.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.link.Link;

public abstract class NodeWithLinks extends Node {

	private static final long serialVersionUID = 1L;

	public static final String SOURCE_CONNECTION = "SourceConnectionAdded";
	public static final String TARGET_CONNECTION = "TargetConnectionAdded";

	public NodeWithLinks(RGB foregroundColor) {
		super(foregroundColor);
	}

	protected List<Link> sourceConnections;
	protected List<Link> targetConnections;
	protected final Map<String, Integer> linksCount = new HashMap<>();

	public boolean addConnections(Link link) {
		if (link.getSourceNode() == this) {
			if (!sourceConnections.contains(link)) {
				if (sourceConnections.add(link)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION, null, link);
					return true;
				}
				return false;
			}
		} else if (link.getTargetNode() == this) {
			if (!targetConnections.contains(link)) {
				if (targetConnections.add(link)) {
					getListeners().firePropertyChange(TARGET_CONNECTION, null, link);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public boolean removeConnection(Link link) {
		if (link.getSourceNode() == this) {
			if (sourceConnections.contains(link)) {
				if (sourceConnections.remove(link)) {
					getListeners().firePropertyChange(SOURCE_CONNECTION, null, link);
					return true;
				}
				return false;
			}
		} else if (link.getTargetNode() == this) {
			if (targetConnections.contains(link)) {
				if (targetConnections.remove(link)) {
					getListeners().firePropertyChange(TARGET_CONNECTION, null, link);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public List<Link> getSourceConnectionsArray() {
		return this.sourceConnections;
	}

	public List<Link> getTargetConnectionsArray() {
		return this.targetConnections;
	}

	public int getLinksCount(String terminal) {
		return linksCount.get(terminal);
	}

	public void increaseLinksCount(String terminal) {
		int count = linksCount.get(terminal);
		count++;
		linksCount.put(terminal, count);
	}

	public void decreaseLinksCount(String terminal) {
		int count = linksCount.get(terminal);
		count--;
		linksCount.put(terminal, count);
	}
}
