package ru.bmstu.rk9.rao.ui.execution;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider.SimulationState;

public abstract class AbstractUIElementUpdatingHandler extends AbstractHandler implements IElementUpdater {
	public enum UIElementType {
		BUILD("Build", "build"), EXECUTE("Execute", "run");

		UIElementType(String description, String task) {
			this.description = description;
			this.task = task;
		}

		private final String description;
		private final String task;

		public String getDescription() {
			return description;
		}

		public String getTask() {
			return task;
		}
	};

	protected AbstractUIElementUpdatingHandler(UIElementType elementType) {
		super();
		this.elementType = elementType;
	}

	private final UIElementType elementType;

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		String message;

		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null) {
			ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow, SimulationState.DISABLED.toString());
			message = elementType.getDescription() + " Rao model: no active workbench window";
		} else {
			IEditorPart activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();

			IProject projectToBuild = BuildJobProvider.getProjectToBuild(activeWorkbenchWindow, activeEditor);

			if (projectToBuild == null) {
				ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
						SimulationState.DISABLED.toString());
				message = elementType.getDescription() + " Rao model: cannot select project to "
						+ elementType.getTask();
			} else {
				message = elementType.getDescription() + " model " + projectToBuild.getName();
				if (ModelExecutionSourceProvider.getSimulationState().equals(SimulationState.DISABLED.toString()))
					ModelExecutionSourceProvider.setSimulationState(activeWorkbenchWindow,
							SimulationState.STOPPED.toString());
			}
		}

		element.setText(message);
		element.setTooltip(message);
	}
}
