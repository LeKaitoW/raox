package ru.bmstu.rk9.rao.ui.process.node;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.bmstu.rk9.rao.lib.process.Queue;
import ru.bmstu.rk9.rao.ui.process.CheckboxPropertyDescriptor;

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
		PropertyDescriptor colorProperty = new ColorPropertyDescriptor(Node.PROPERTY_COLOR, "Color");
		properties.add(colorProperty);
		PropertyDescriptor nameProperty = new CheckboxPropertyDescriptor(BlockNode.PROPERTY_NAME, "Show name");
		properties.add(nameProperty);

		if (node instanceof BlockNodeWithResource) {
			properties.add(new ComboBoxPropertyDescriptor(BlockNodeWithResource.PROPERTY_RESOURCE, "Resource",
					((BlockNodeWithResource) node).getResourcesNames().stream().toArray(String[]::new)));
		}

		if (node instanceof BlockNodeWithInterval) {
			properties.add(new TextPropertyDescriptor(BlockNodeWithInterval.PROPERTY_INTERVAL,
					((BlockNodeWithInterval) node).getIntervalName()));
		}

		if (node instanceof BlockNodeWithProbability) {
			properties.add(new TextPropertyDescriptor(BlockNodeWithProbability.PROPERTY_PROBABILITY, "Probability"));
		}

		if (node instanceof BlockNodeWithCapacity) {
			properties.add(new TextPropertyDescriptor(BlockNodeWithCapacity.PROPERTY_CAPACITY, "Capacity"));
			properties.add(new ComboBoxPropertyDescriptor(BlockNodeWithCapacity.PROPERTY_QUEUEING, "Queueing",
					Queue.getQueueingArray()));
		}

		return properties.stream().toArray(IPropertyDescriptor[]::new);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals(Node.PROPERTY_COLOR)) {
			return node.getColor();
		}
		if (id.equals(BlockNode.PROPERTY_NAME) && node instanceof BlockNode) {
			return ((BlockNode) node).nameIsVisible();
		}
		if (id.equals(BlockNodeWithResource.PROPERTY_RESOURCE) && node instanceof BlockNodeWithResource) {
			return ((BlockNodeWithResource) node).getResourceNameIndex();
		}
		if (id.equals(BlockNodeWithInterval.PROPERTY_INTERVAL) && node instanceof BlockNodeWithInterval) {
			return ((BlockNodeWithInterval) node).getInterval();
		}
		if (id.equals(BlockNodeWithProbability.PROPERTY_PROBABILITY) && node instanceof BlockNodeWithProbability) {
			return ((BlockNodeWithProbability) node).getProbability();
		}
		if (id.equals(BlockNodeWithCapacity.PROPERTY_CAPACITY) && node instanceof BlockNodeWithCapacity) {
			return ((BlockNodeWithCapacity) node).getCapacity();
		}
		if (id.equals(BlockNodeWithCapacity.PROPERTY_QUEUEING) && node instanceof BlockNodeWithCapacity) {
			return ((BlockNodeWithCapacity) node).getQueueingIndex();
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
		if (id.equals(Node.PROPERTY_COLOR)) {
			RGB newColor = (RGB) value;
			node.setColor(newColor);
		} else if (id.equals(BlockNode.PROPERTY_NAME) && node instanceof BlockNode) {
			((BlockNode) node).setNameVisible((boolean) value);
		} else if (id.equals(BlockNodeWithResource.PROPERTY_RESOURCE) && node instanceof BlockNodeWithResource) {
			((BlockNodeWithResource) node).setResourceName((int) value);
		} else if (id.equals(BlockNodeWithInterval.PROPERTY_INTERVAL) && node instanceof BlockNodeWithInterval) {
			((BlockNodeWithInterval) node).setInterval((String) value);
		} else if (id.equals(BlockNodeWithProbability.PROPERTY_PROBABILITY)
				&& node instanceof BlockNodeWithProbability) {
			((BlockNodeWithProbability) node).setProbability((String) value);
		} else if (id.equals(BlockNodeWithCapacity.PROPERTY_CAPACITY) && node instanceof BlockNodeWithCapacity) {
			((BlockNodeWithCapacity) node).setCapacity((String) value);
		} else if (id.equals(BlockNodeWithCapacity.PROPERTY_QUEUEING) && node instanceof BlockNodeWithCapacity) {
			((BlockNodeWithCapacity) node).setQueueing((int) value);
		}
	}
}
