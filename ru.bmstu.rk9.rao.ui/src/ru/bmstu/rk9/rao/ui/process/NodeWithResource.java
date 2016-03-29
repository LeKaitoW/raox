package ru.bmstu.rk9.rao.ui.process;

import java.util.List;

import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.model.Model;

public abstract class NodeWithResource extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_RESOURCE = "NodeResource";

	protected String resourceName = "";

	public NodeWithResource(RGB foregroundColor) {
		super(foregroundColor);
	}

	public void setResourceName(int index) {
		String oldName = this.resourceName;
		this.resourceName = getResourcesNames().get(index);
		getListeners().firePropertyChange(PROPERTY_RESOURCE, oldName, resourceName);
	}

	public Integer getResourceNameIndex() {
		return getResourcesNames().indexOf(resourceName);
	}

	public List<String> getResourcesNames() {
		EResourceRetriever resourceRetriever = ((Model) getParent()).getResourceRetriever();
		return resourceRetriever.getResourcesNames();
	}
}
