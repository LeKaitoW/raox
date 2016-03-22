package ru.bmstu.rk9.rao.ui.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.rao.ResourceDeclaration;
import ru.bmstu.rk9.rao.ui.execution.BuildUtil;
import ru.bmstu.rk9.rao.ui.process.model.Model;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigurator;

public abstract class NodeWithResource extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_RESOURCE = "NodeResource";

	protected String resourceName = "";

	public NodeWithResource(RGB backgroundColor) {
		super(backgroundColor);
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
		IProject project = resourceRetriever.project;
		final ResourceSet resourceSet = resourceRetriever.resourceSetProvider.get(project);
		if (resourceSet == null)
			return new ArrayList<String>();

		List<String> resourcesNames = new ArrayList<String>();
		for (IResource resource : BuildUtil.getAllFilesInProject(project, "rao")) {
			URI uri = BuildUtil.getURI(resource);
			Resource loadedResource = resourceSet.getResource(uri, true);
			if (loadedResource == null)
				continue;
			List<ResourceDeclaration> resources = SerializationConfigurator
					.filterAllContents(loadedResource.getAllContents(), ResourceDeclaration.class);
			for (ResourceDeclaration declaration : resources) {
				resourcesNames.add(declaration.getName());
			}
		}
		return resourcesNames;
	}
}
