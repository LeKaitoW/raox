package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

public class NodeWithProperty extends Node implements IAdaptable {

	private static final long serialVersionUID = 1;

	private RGB color;
	private boolean nameIsVisible = true;
	private transient IPropertySource propertySource = null;

	public NodeWithProperty(RGB backgroundColor) {
		super(backgroundColor);
	}

	public void setColor(RGB newColor) {
		RGB oldColor = this.color;
		this.color = newColor;
		getListeners().firePropertyChange(PROPERTY_COLOR, oldColor, newColor);
	}

	public boolean nameIsVisible() {
		return nameIsVisible;
	}

	public void setNameVisible(boolean visible) {
		boolean oldVisible = this.nameIsVisible;
		this.nameIsVisible = visible;
		getListeners().firePropertyChange(PROPERTY_NAME, oldVisible, visible);
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
