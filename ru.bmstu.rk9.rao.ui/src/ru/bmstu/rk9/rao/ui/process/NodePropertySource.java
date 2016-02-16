package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class NodePropertySource implements IPropertySource {

	private NodeWithProperty node;
	private IPropertyDescriptor[] properties;

	public NodePropertySource(NodeWithProperty node) {
		this.node = node;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor colorProperty = new ColorPropertyDescriptor(NodeWithProperty.PROPERTY_COLOR, "Color");
		PropertyDescriptor nameProperty = new CheckboxPropertyDescriptor(NodeWithProperty.PROPERTY_NAME, "Show name");
		properties = new IPropertyDescriptor[] { colorProperty, nameProperty };
		return properties;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(NodeWithProperty.PROPERTY_COLOR)) {
			return node.getColor();
		} else if (id.equals(NodeWithProperty.PROPERTY_NAME)) {
			return node.nameIsVisible();
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(NodeWithProperty.PROPERTY_COLOR)) {
			RGB newColor = (RGB) value;
			node.setColor(newColor);
		} else if (id.equals(NodeWithProperty.PROPERTY_NAME)) {
			node.setNameVisible((boolean) value);
		}
	}
}
