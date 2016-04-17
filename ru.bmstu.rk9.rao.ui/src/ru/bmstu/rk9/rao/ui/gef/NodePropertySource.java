package ru.bmstu.rk9.rao.ui.gef;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class NodePropertySource implements IPropertySource {

	private Node node;

	public NodePropertySource(Node node) {
		this.node = node;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
		node.createProperties(properties);
		return properties.stream().toArray(IPropertyDescriptor[]::new);
	}

	@Override
	public Object getPropertyValue(Object id) {
		return node.getPropertyValue(id);
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		node.setPropertyValue(id, value);
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}
}
