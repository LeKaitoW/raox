package ru.bmstu.rk9.rao.ui.gef;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public abstract class Node implements Serializable, IAdaptable {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_ADD = "NodeAddChild";
	public static final String PROPERTY_REMOVE = "NodeRemoveChild";
	public static final String PROPERTY_CONSTRAINT = "NodeConstraint";

	private Node parent = null;
	private List<Node> children = new ArrayList<Node>();
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	private Rectangle constraint = new Rectangle(10, 10, 100, 100);

	private transient IPropertySource propertySource = null;

	public final boolean addChild(Node child) {
		boolean isAdded = this.children.add(child);
		if (isAdded) {
			child.setParent(this);
			getListeners().firePropertyChange(PROPERTY_ADD, null, child);
			child.assignedToParent();
		}
		return isAdded;
	}

	public final boolean removeChild(Node child) {
		boolean isRemoved = this.children.remove(child);
		if (isRemoved)
			getListeners().firePropertyChange(PROPERTY_REMOVE, child, null);
		return isRemoved;
	}

	public final List<Node> getChildren() {
		return children;
	}

	public final boolean contains(Node child) {
		return children.contains(child);
	}

	public final Node getParent() {
		return parent;
	}

	public final void setParent(Node parent) {
		this.parent = parent;
	}

	public final Node getRoot() {
		Node root = this;
		while (root.getParent() != null)
			root = root.getParent();
		return root;
	}

	public final PropertyChangeSupport getListeners() {
		return listeners;
	}

	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	public final Rectangle getConstraint() {
		return constraint;
	}

	public final void setConstraint(Rectangle constraint) {
		Rectangle previousValue = this.constraint;
		this.constraint = constraint;
		getListeners().firePropertyChange(PROPERTY_CONSTRAINT, previousValue, constraint);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null) {
				propertySource = new NodePropertySource(this);
			}
			return propertySource;
		}
		return null;
	}

	public abstract void createProperties(List<PropertyDescriptor> properties);

	public abstract Object getPropertyValue(String propertyName);

	public abstract void setPropertyValue(String propertyName, Object value);

	public void assignedToParent() {
	}

	public void onDelete() {
	}
}
