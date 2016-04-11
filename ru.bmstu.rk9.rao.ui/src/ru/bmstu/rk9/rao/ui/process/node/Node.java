package ru.bmstu.rk9.rao.ui.process.node;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

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
	public static final String NODE_MARKER = "NodeID";

	protected RGB color;
	protected int ID;

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

	public void setID(int ID) {
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
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
}
