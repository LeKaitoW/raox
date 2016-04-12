package ru.bmstu.rk9.rao.ui.process.node;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.model.ModelNode;

public abstract class BlockNodeWithResource extends BlockNode {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_RESOURCE = "NodeResource";

	protected String resourceName = "";

	public void setResourceName(int index) {
		String oldName = this.resourceName;
		this.resourceName = getResourcesNames().get(index);
		getListeners().firePropertyChange(PROPERTY_RESOURCE, oldName, resourceName);
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
}
