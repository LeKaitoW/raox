package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.swt.graphics.RGB;

public abstract class NodeWithResource extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_RESOURCE = "NodeResource";

	protected String resourceName = "";

	public NodeWithResource(RGB backgroundColor) {
		super(backgroundColor);
	}

	public void setResourceName(String resourceName) {
		String oldName = this.resourceName;
		this.resourceName = resourceName;
		getListeners().firePropertyChange(PROPERTY_RESOURCE, oldName, resourceName);
	}

	public String getResourceName() {
		return resourceName;
	}
}
