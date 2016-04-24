package ru.bmstu.rk9.rao.ui.process.model;

import java.util.List;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;

public class ModelNode extends Node {

	private static final long serialVersionUID = 1;

	public static String name = "Model";
	private transient EResourceRetriever resourceRetriever;

	public final EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}

	public final void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		return null;
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
	}
}
