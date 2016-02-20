package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.IPropertySource;

import ru.bmstu.rk9.rao.ui.process.link.ProcessLink;

public class NodeWithProperty extends Node implements IAdaptable {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_COLOR = "NodeColor";
	public static final String PROPERTY_NAME = "ShowNodeName";

	private boolean nameIsVisible = true;
	private transient IPropertySource propertySource = null;

	public NodeWithProperty(RGB backgroundColor) {
		super(backgroundColor);
		this.sourceConnections = new ArrayList<ProcessLink>();
		this.targetConnections = new ArrayList<ProcessLink>();
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
