package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.gef.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelNode;

public abstract class BlockNodeWithResource extends BlockNode {

	private static final long serialVersionUID = 1;

	private static final String PROPERTY_RESOURCE_NAME_INDEX = "ResourceNameIndex";

	protected String resourceName = "";

	public void setResourceNameIndex(int index) {
		String previousValue = this.resourceName;
		this.resourceName = getResourcesNames().get(index);
		getListeners().firePropertyChange(PROPERTY_RESOURCE_NAME_INDEX, previousValue, resourceName);
	}

	public Integer getResourceNameIndex() {
		return getResourcesNames().indexOf(resourceName);
	}

	public List<String> getResourcesNames() {
		EResourceRetriever resourceRetriever = ((ProcessModelNode) getParent()).getResourceRetriever();
		return resourceRetriever.getResourcesNames();
	}

	public void validateResource(IResource file) throws CoreException {
		if (!getResourcesNames().contains(resourceName))
			createProblemMarker(file, "Wrong resource", IMarker.SEVERITY_ERROR);
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new ComboBoxPropertyDescriptor(PROPERTY_RESOURCE_NAME_INDEX, "Resource",
				getResourcesNames().stream().toArray(String[]::new)));
	}

	@Override
	public Object getPropertyValue(String propertyName) {

		switch (propertyName) {
		case PROPERTY_RESOURCE_NAME_INDEX:
			return getResourceNameIndex();
		}

		return super.getPropertyValue(propertyName);
	}

	@Override
	public void setPropertyValue(String propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		switch (propertyName) {
		case PROPERTY_RESOURCE_NAME_INDEX:
			setResourceNameIndex((int) value);
			break;
		}
	}
}
