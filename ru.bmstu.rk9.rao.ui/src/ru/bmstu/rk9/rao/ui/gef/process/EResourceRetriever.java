package ru.bmstu.rk9.rao.ui.gef.process;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

import ru.bmstu.rk9.rao.rao.FunctionDeclaration;
import ru.bmstu.rk9.rao.rao.ResourceDeclaration;
import ru.bmstu.rk9.rao.ui.execution.BuildUtil;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigurator;

public class EResourceRetriever {

	private final IResourceSetProvider resourceSetProvider;
	private final IProject project;

	public EResourceRetriever(IResourceSetProvider resourceSetProvider, IProject project) {
		this.resourceSetProvider = resourceSetProvider;
		this.project = project;
	}

	public List<String> getResourcesNames() {
		IProject project = this.project;
		final ResourceSet resourceSet = this.resourceSetProvider.get(project);
		List<String> resourcesNames = new ArrayList<String>();
		if (resourceSet == null)
			return resourcesNames;

		for (IResource resource : BuildUtil.getAllFilesInProject(project, "rao")) {
			String raoFileName = resource.getName();
			raoFileName = raoFileName.substring(0, raoFileName.lastIndexOf("."));
			String modelClassName = project.getName() + "." + raoFileName;

			URI uri = BuildUtil.getURI(resource);
			Resource loadedResource = resourceSet.getResource(uri, true);
			if (loadedResource == null)
				continue;
			List<ResourceDeclaration> resources = SerializationConfigurator
					.filterAllContents(loadedResource.getAllContents(), ResourceDeclaration.class);
			for (ResourceDeclaration declaration : resources) {
				resourcesNames.add(modelClassName + "." + declaration.getName());
			}
		}
		return resourcesNames;
	}

	public List<String> getBooleanFunctionsNames() {
		IProject project = this.project;
		final ResourceSet resourceSet = this.resourceSetProvider.get(project);
		List<String> functionsNames = new ArrayList<String>();
		if (resourceSet == null)
			return functionsNames;

		for (IResource resource : BuildUtil.getAllFilesInProject(project, "rao")) {
			String raoFileName = resource.getName();
			raoFileName = raoFileName.substring(0, raoFileName.lastIndexOf("."));

			URI uri = BuildUtil.getURI(resource);
			Resource loadedResource = resourceSet.getResource(uri, true);
			if (loadedResource == null)
				continue;
			String modelClassName = project.getName() + "." + raoFileName;
			List<FunctionDeclaration> functions = SerializationConfigurator
					.filterAllContents(loadedResource.getAllContents(), FunctionDeclaration.class);
			for (FunctionDeclaration declaration : functions) {
				if (!declaration.getType().getSimpleName().equals("boolean"))
					continue;
				if (!declaration.getParameters().isEmpty())
					continue;
				functionsNames.add(modelClassName + "." + declaration.getName());
			}
		}
		return functionsNames;
	}
}
