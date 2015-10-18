package ru.bmstu.rk9.rao.ui.build;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class BuildHandler extends AbstractHandler {
	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;

	@Inject
	IResourceSetProvider resourceSetProvider;

	@Inject
	EclipseOutputConfigurationProvider outputConfigurationProvider;

	@Inject
	DefaultResourceUIValidatorExtension validatorExtension;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ModelBuilder.build(event, fileAccessProvider.get(),
				resourceSetProvider, outputConfigurationProvider,
				validatorExtension).schedule();
		return null;
	}
}
