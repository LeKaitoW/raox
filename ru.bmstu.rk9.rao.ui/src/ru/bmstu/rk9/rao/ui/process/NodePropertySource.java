package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class NodePropertySource implements IPropertySource {

	private Node node;
	private IPropertyDescriptor[] properties;

	public NodePropertySource(Node node) {
		this.node = node;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		PropertyDescriptor colorProperty = new ColorPropertyDescriptor(
				Node.PROPERTY_COLOR, "Color");
		PropertyDescriptor nameProperty = new CheckboxPropertyDescriptor(
				Node.PROPERTY_NAME, "Show name");
		properties = new IPropertyDescriptor[] { colorProperty, nameProperty };
		return properties;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(Node.PROPERTY_COLOR)) {
			return node.getColor().getRGB();
		} else if (id.equals(Node.PROPERTY_NAME)) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(Node.PROPERTY_COLOR)) {
			Color newColor = new Color(null, (RGB) value);
			node.setColor(newColor);
		} else if (id.equals(Node.PROPERTY_NAME)) {
			node.setNameVisible((boolean) value);
		}
	}
}
