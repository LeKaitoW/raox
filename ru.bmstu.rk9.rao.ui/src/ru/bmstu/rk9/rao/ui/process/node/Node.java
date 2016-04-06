package ru.bmstu.rk9.rao.ui.process.node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.link.ProcessLink;

public abstract class Node implements Serializable {

	private static final long serialVersionUID = 1;

	private String name;
	private Rectangle constraint;
	private List<Node> children;
	private Node parent;
	private PropertyChangeSupport listeners;
	public static final String PROPERTY_CONSTRAINT = "NodeConstraint";
	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	public static final String SOURCE_CONNECTION = "SourceConnectionAdded";
	public static final String TARGET_CONNECTION = "TargetConnectionAdded";

	protected RGB color;
	protected List<ProcessLink> sourceConnections;
	protected List<ProcessLink> targetConnections;

	protected final Map<String, Integer> linksCount = new HashMap<>();

	public Node(RGB foregroundColor) {
		this.name = "Unknown";
		this.constraint = new Rectangle(10, 10, 100, 100);
		this.children = new ArrayList<Node>();
		this.parent = null;
		this.listeners = new PropertyChangeSupport(this);
		this.color = foregroundColor;
	}

	public RGB getColor() {
		return color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public final void setConstraint(Rectangle constraint) {
		Rectangle previousConstraint = this.constraint;
		this.constraint = constraint;
		getListeners().firePropertyChange(PROPERTY_CONSTRAINT, previousConstraint, constraint);
	}

	public final Rectangle getConstraint() {
		return constraint;
	}

	public boolean addChild(Node child) {
		boolean isAdded = this.children.add(child);
		if (isAdded) {
			child.setParent(this);
			getListeners().firePropertyChange(PROPERTY_ADD, null, child);
		}
		return isAdded;
	}

	public boolean removeChild(Node child) {
		boolean isRemoved = this.children.remove(child);
		if (isRemoved)
			getListeners().firePropertyChange(PROPERTY_REMOVE, child, null);
		return isRemoved;
	}

	public List<Node> getChildren() {
		return this.children;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public Node getParent() {
		return this.parent;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	public PropertyChangeSupport getListeners() {
		return listeners;
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	public boolean contains(Node child) {
		return children.contains(child);
	}

	public boolean addConnections(ProcessLink link) {
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

	public boolean removeConnection(ProcessLink link) {
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

	public List<ProcessLink> getSourceConnectionsArray() {
		return this.sourceConnections;
	}

	public List<ProcessLink> getTargetConnectionsArray() {
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

	public abstract BlockConverterInfo createBlock();
}
