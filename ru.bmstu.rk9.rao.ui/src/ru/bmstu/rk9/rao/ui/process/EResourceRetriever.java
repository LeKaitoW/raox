package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.core.resources.IProject;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

public class EResourceRetriever {

	public final IResourceSetProvider resourceSetProvider;
	public final IProject project;

	public EResourceRetriever(IResourceSetProvider resourceSetProvider, IProject project) {
		this.resourceSetProvider = resourceSetProvider;
		this.project = project;
	}
}
