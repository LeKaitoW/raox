package ru.bmstu.rk9.rao.ui.process.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.link.Link;

public abstract class NodeWithLinks extends Node {

	private static final long serialVersionUID = 1L;

	public static final String SOURCE_LINK_UPDATED = "SourceLinkUpdated";
	public static final String TARGET_LINK_UPDATED = "TargetLinkUpdated";

	public NodeWithLinks(RGB foregroundColor) {
		super(foregroundColor);
	}

	protected List<Link> sourceLinks;
	protected List<Link> targetLinks;
	private final Map<String, Integer> docks = new HashMap<>();

	public final boolean addLink(Link link) {
		if (link.getSourceNode() == this) {
			if (!sourceLinks.contains(link)) {
				if (sourceLinks.add(link)) {
					getListeners().firePropertyChange(SOURCE_LINK_UPDATED, null, link);
					return true;
				}
				return false;
			}
		} else if (link.getTargetNode() == this) {
			if (!targetLinks.contains(link)) {
				if (targetLinks.add(link)) {
					getListeners().firePropertyChange(TARGET_LINK_UPDATED, null, link);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final boolean removeLink(Link link) {
		if (link.getSourceNode() == this) {
			if (sourceLinks.contains(link)) {
				if (sourceLinks.remove(link)) {
					getListeners().firePropertyChange(SOURCE_LINK_UPDATED, null, link);
					return true;
				}
				return false;
			}
		} else if (link.getTargetNode() == this) {
			if (targetLinks.contains(link)) {
				if (targetLinks.remove(link)) {
					getListeners().firePropertyChange(TARGET_LINK_UPDATED, null, link);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	public final List<Link> getSourceLinks() {
		return sourceLinks;
	}

	public final List<Link> getTargetLinks() {
		return targetLinks;
	}

	public final int getDocksCount(String dock) {
		return docks.get(dock);
	}

	public final void registerDock(String dock) {
		docks.put(dock, 0);
	}

	public final void captureDock(String dock) {
		int count = docks.get(dock);
		count++;
		docks.put(dock, count);
	}

	public final void releaseDock(String dock) {
		int count = docks.get(dock);
		count--;
		docks.put(dock, count);
	}
}
