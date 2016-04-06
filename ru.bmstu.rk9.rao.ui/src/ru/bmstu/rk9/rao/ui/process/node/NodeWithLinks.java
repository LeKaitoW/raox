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
	protected final Map<String, Integer> linksCount = new HashMap<>();

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

	public final int getLinksCount(String terminal) {
		return linksCount.get(terminal);
	}

	public final void increaseLinksCount(String terminal) {
		int count = linksCount.get(terminal);
		count++;
		linksCount.put(terminal, count);
	}

	public final void decreaseLinksCount(String terminal) {
		int count = linksCount.get(terminal);
		count--;
		linksCount.put(terminal, count);
	}
}
