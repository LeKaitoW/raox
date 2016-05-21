package ru.bmstu.rk9.rao.ui.execution;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class StartExecutionHandler extends AbstractUIElementUpdatingHandler {
	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;

	@Inject
	private IResourceSetProvider resourceSetProvider;

	@Inject
	private EclipseOutputConfigurationProvider outputConfigurationProvider;

	@Inject
	DefaultResourceUIValidatorExtension validatorExtension;

	public StartExecutionHandler() {
		super(UIElementType.EXECUTE);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);

		ExecutionManager executionManager = new ExecutionManager(activeEditor, activeWorkbenchWindow,
				fileAccessProvider.get(), resourceSetProvider, outputConfigurationProvider, validatorExtension);
		executionManager.execute(false);

		return null;
	}
}
