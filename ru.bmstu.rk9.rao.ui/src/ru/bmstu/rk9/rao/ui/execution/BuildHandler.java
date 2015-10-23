package ru.bmstu.rk9.rao.ui.execution;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.validation.DefaultResourceUIValidatorExtension;

import ru.bmstu.rk9.rao.IMultipleResourceGenerator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class BuildHandler extends AbstractHandler implements IElementUpdater {
	@Inject
	private IMultipleResourceGenerator generator;

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
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);

		ExecutionManager executionManager = new ExecutionManager(activeEditor,
				activeWorkbenchWindow, fileAccessProvider.get(),
				resourceSetProvider, outputConfigurationProvider, generator,
				validatorExtension);
		executionManager.execute(true);
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;

		IEditorPart activeEditor = activeWorkbenchWindow.getActivePage()
				.getActiveEditor();

		IProject projectToBuild = BuildJobProvider.getProjectToBuild(
				activeWorkbenchWindow, activeEditor);
		String message;
		if (projectToBuild == null)
			message = "Build rao model: cannot choose project to build";
		else
			message = "Build model " + projectToBuild.getName();

		element.setText(message);
		element.setTooltip(message);
	}
}
