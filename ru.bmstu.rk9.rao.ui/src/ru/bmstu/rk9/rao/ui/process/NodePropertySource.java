package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class NodePropertySource implements IPropertySource {

	private NodeWithProperty node;

	public NodePropertySource(NodeWithProperty node) {
		this.node = node;
	}

	@Override
	public Object getEditableValue() {
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();
		PropertyDescriptor colorProperty = new ColorPropertyDescriptor(NodeWithProperty.PROPERTY_COLOR, "Color");
		properties.add(colorProperty);
		PropertyDescriptor nameProperty = new CheckboxPropertyDescriptor(NodeWithProperty.PROPERTY_NAME, "Show name");
		properties.add(nameProperty);

		if (node instanceof NodeWithResource) {
			properties.add(new TextPropertyDescriptor(NodeWithResource.PROPERTY_RESOURCE, "Resource"));
		}

		if (node instanceof NodeWithInterval) {
			properties.add(new TextPropertyDescriptor(NodeWithInterval.PROPERTY_INTERVAL,
					((NodeWithInterval) node).getIntervalName()));
		}

		if (node instanceof NodeWithProbability) {
			properties.add(new TextPropertyDescriptor(NodeWithProbability.PROPERTY_PROBABILITY, "Probability"));
		}

		if (node instanceof NodeWithCapacity) {
			properties.add(new TextPropertyDescriptor(NodeWithCapacity.PROPERTY_CAPACITY, "Capacity"));
		}

		return properties.stream().toArray(IPropertyDescriptor[]::new);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(NodeWithProperty.PROPERTY_COLOR)) {
			return node.getColor();
		}
		if (id.equals(NodeWithProperty.PROPERTY_NAME)) {
			return node.nameIsVisible();
		}
		if (id.equals(NodeWithResource.PROPERTY_RESOURCE) && node instanceof NodeWithResource) {
			return ((NodeWithResource) node).getResourceName();
		}
		if (id.equals(NodeWithInterval.PROPERTY_INTERVAL) && node instanceof NodeWithInterval) {
			return ((NodeWithInterval) node).getInterval();
		}
		if (id.equals(NodeWithProbability.PROPERTY_PROBABILITY) && node instanceof NodeWithProbability) {
			return ((NodeWithProbability) node).getProbability();
		}
		if (id.equals(NodeWithCapacity.PROPERTY_CAPACITY) && node instanceof NodeWithCapacity) {
			return ((NodeWithCapacity) node).getCapacity();
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
		} else if (id.equals(NodeWithResource.PROPERTY_RESOURCE) && node instanceof NodeWithResource) {
			((NodeWithResource) node).setResourceName((String) value);
		} else if (id.equals(NodeWithInterval.PROPERTY_INTERVAL) && node instanceof NodeWithInterval) {
			((NodeWithInterval) node).setInterval((String) value);
		} else if (id.equals(NodeWithProbability.PROPERTY_PROBABILITY) && node instanceof NodeWithProbability) {
			((NodeWithProbability) node).setProbability((String) value);
		} else if (id.equals(NodeWithCapacity.PROPERTY_CAPACITY) && node instanceof NodeWithCapacity) {
			((NodeWithCapacity) node).setCapacity((String) value);
		}
	}
}
