package ru.bmstu.rk9.rao.ui.process.node;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.model.ModelNode;

public abstract class BlockNodeWithResource extends BlockNode {

	private static final long serialVersionUID = 1;

	protected static final String PROPERTY_RESOURCE_NAME_INDEX = "ResourceNameIndex";

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
		EResourceRetriever resourceRetriever = ((ModelNode) getParent()).getResourceRetriever();
		return resourceRetriever.getResourcesNames();
	}

	public void validateResource(IResource file) throws CoreException {
		if (!getResourcesNames().contains(resourceName)) {
			IMarker marker = file.createMarker(BlockNode.PROCESS_MARKER);
			marker.setAttribute(IMarker.MESSAGE, "Wrong resource");
			marker.setAttribute(IMarker.LOCATION, getName());
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(NODE_MARKER, getID());
		}
	}

	@Override
	public void createProperties(List<PropertyDescriptor> properties) {
		super.createProperties(properties);

		properties.add(new ComboBoxPropertyDescriptor(PROPERTY_RESOURCE_NAME_INDEX, "Resource",
				getResourcesNames().stream().toArray(String[]::new)));
	}

	@Override
	public Object getPropertyValue(Object propertyName) {
		Object value = super.getPropertyValue(propertyName);
		if (value != null)
			return value;

		if (propertyName.equals(PROPERTY_RESOURCE_NAME_INDEX))
			return getResourceNameIndex();

		return null;
	}

	@Override
	public void setPropertyValue(Object propertyName, Object value) {
		super.setPropertyValue(propertyName, value);

		if (propertyName.equals(PROPERTY_RESOURCE_NAME_INDEX))
			setResourceNameIndex((int) value);
	}
}
