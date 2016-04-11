package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

public class Node extends ru.bmstu.rk9.rao.ui.gef.Node implements IAdaptable {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_CONSTRAINT = "NodeConstraint";
	public static final String PROPERTY_COLOR = "NodeColor";
	public static final String PROPERTY_NAME = "ShowNodeName";

	private transient IPropertySource propertySource = null;

	private String name;
	private boolean nameIsVisible = true;
	protected RGB color;
	private Rectangle constraint;

	public Node(RGB color) {
		this.name = "Unknown";
		this.color = color;
		this.constraint = new Rectangle(10, 10, 100, 100);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean nameIsVisible() {
		return nameIsVisible;
	}

	public void setNameVisible(boolean visible) {
		boolean oldVisible = this.nameIsVisible;
		this.nameIsVisible = visible;
		getListeners().firePropertyChange(PROPERTY_NAME, oldVisible, visible);
	}

	public RGB getColor() {
		return color;
	}

	public void setColor(RGB color) {
		RGB oldColor = this.color;
		this.color = color;
		getListeners().firePropertyChange(PROPERTY_COLOR, oldColor, color);
	}

	public final void setConstraint(Rectangle constraint) {
		Rectangle previousConstraint = this.constraint;
		this.constraint = constraint;
		getListeners().firePropertyChange(PROPERTY_CONSTRAINT, previousConstraint, constraint);
	}

	public final Rectangle getConstraint() {
		return constraint;
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
}
