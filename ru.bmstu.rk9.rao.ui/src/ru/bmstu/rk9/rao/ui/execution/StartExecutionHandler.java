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
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider.SimulationState;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class StartExecutionHandler extends AbstractHandler implements
		IElementUpdater {
	@Inject
	private IMultipleResourceGenerator generator;

	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileAccessProvider;

	@Inject
	private IResourceSetProvider resourceSetProvider;

	@Inject
	private EclipseOutputConfigurationProvider outputConfigurationProvider;

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
		executionManager.execute(false);

		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		String message;

		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null) {
			ModelExecutionSourceProvider.setSimulationState(
					activeWorkbenchWindow, SimulationState.DISABLED.toString());
			message = "Execute Rao model: no active workbench window";
		} else {
			IEditorPart activeEditor = activeWorkbenchWindow.getActivePage()
					.getActiveEditor();

			IProject projectToBuild = BuildJobProvider.getProjectToBuild(
					activeWorkbenchWindow, activeEditor);

			if (projectToBuild == null) {
				ModelExecutionSourceProvider.setSimulationState(
						activeWorkbenchWindow,
						SimulationState.DISABLED.toString());
				message = "Execute Rao model: cannot choose project to run";
			} else {
				message = "Execute model " + projectToBuild.getName();
				if (ModelExecutionSourceProvider.getSimulationState().equals(
						SimulationState.DISABLED.toString()))
					ModelExecutionSourceProvider.setSimulationState(
							activeWorkbenchWindow,
							SimulationState.STOPPED.toString());
			}
		}

		element.setText(message);
		element.setTooltip(message);
	}
}
