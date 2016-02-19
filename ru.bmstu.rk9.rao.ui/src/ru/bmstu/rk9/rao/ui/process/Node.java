package ru.bmstu.rk9.rao.ui.process;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

public class Node implements Serializable {

	private static final long serialVersionUID = 1;

	private String name;
	private Rectangle layout;
	private List<Node> children;
	private Node parent;
	private PropertyChangeSupport listeners;
	public static final String PROPERTY_LAYOUT = "NodeLayout";
	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	public static final String SOURCE_CONNECTION = "SourceConnectionAdded";
	public static final String TARGET_CONNECTION = "TargetConnectionAdded";

	protected RGB color;
	protected List<ProcessLink> sourceConnections;
	protected List<ProcessLink> targetConnections;

	public Node(RGB backgroundColor) {
		this.name = "Unknown";
		this.layout = new Rectangle(10, 10, 100, 100);
		this.children = new ArrayList<Node>();
		this.parent = null;
		this.listeners = new PropertyChangeSupport(this);
		this.color = backgroundColor;
	}

	public RGB getColor() {
		return color;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setLayout(Rectangle newLayout) {
		Rectangle oldLayout = this.layout;
		this.layout = newLayout;
		getListeners().firePropertyChange(PROPERTY_LAYOUT, oldLayout, newLayout);
	}

	public Rectangle getLayout() {
		return this.layout;
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
}
