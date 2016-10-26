package ru.bmstu.rk9.rao.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.ISourceProviderService;
import org.osgi.framework.BundleContext;

import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.internal.RaoActivator;
import ru.bmstu.rk9.rao.ui.plot.PlotView;
import ru.bmstu.rk9.rao.ui.results.ResultsView;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rao.ui.simulation.ModelExecutionSourceProvider.SimulationState;
import ru.bmstu.rk9.rao.ui.simulation.RuntimeComponents;
import ru.bmstu.rk9.rao.ui.simulation.SetSimulationScaleHandler;
import ru.bmstu.rk9.rao.ui.simulation.SpeedSelectionToolbar;

public class RaoActivatorExtension extends RaoActivator {
	private MessageDialog closeDialog = new MessageDialog(Display.getDefault().getActiveShell(), "Error", null,
			"Simulation should be stopped prior to shutdown!", MessageDialog.ERROR,
			new String[] { "Force Shutdown", "OK" }, 1);

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		preinitializeUiComponents();

		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandService = workbench.getService(ICommandService.class);

		workbench.addWorkbenchListener(workbenchListener);
		commandService.addExecutionListener(commandExecutionListener);
		workbench.addWindowListener(new IWindowListener() {
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				IPartService partService = window.getPartService();
				if (partService == null)
					return;
				partService.addPartListener(partListener);
			}

			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
			}

			@Override
			public void windowClosed(IWorkbenchWindow window) {
				IPartService partService = window.getPartService();
				if (partService == null)
					return;
				partService.removePartListener(partListener);
			}

			@Override
			public void windowActivated(IWorkbenchWindow window) {
			}
		});
	}

	private void preinitializeUiComponents() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("ru.bmstu.rk9.rao.ui");
		RuntimeComponents.initialize();
		SpeedSelectionToolbar.setSpeed(prefs.getInt("SimulationSpeed", 100));
		SetSimulationScaleHandler.setSimulationScale(prefs.getDouble("SimulationScale", 3600d));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		deinitializeUiComponents();

		super.stop(context);
	}

	private void deinitializeUiComponents() {
		IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("ru.bmstu.rk9.rao.ui");

		prefs.putInt("SimulationSpeed", SpeedSelectionToolbar.getSpeed());
		prefs.putDouble("SimulationScale", SetSimulationScaleHandler.getSimulationScale());

		int animationFrameListSize = AnimationView.getFrameListSize();
		if (animationFrameListSize != SWT.DEFAULT)
			prefs.putInt("AnimationViewFrameListSize", animationFrameListSize);

		ResultsView.savePreferences();

		RuntimeComponents.deinitialize();
	}

	private final IWorkbenchListener workbenchListener = new IWorkbenchListener() {
		@Override
		public boolean preShutdown(IWorkbench workbench, boolean forced) {
			ISourceProviderService sourceProviderService = workbench.getActiveWorkbenchWindow()
					.getService(ISourceProviderService.class);
			ModelExecutionSourceProvider sourceProvider = (ModelExecutionSourceProvider) sourceProviderService
					.getSourceProvider(ModelExecutionSourceProvider.ModelExecutionKey);

			final AtomicInteger result = new AtomicInteger(0);
			if (sourceProvider.getCurrentState()
					.get(ModelExecutionSourceProvider.ModelExecutionKey) == SimulationState.RUNNING.toString()) {
				workbench.getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						result.set(closeDialog.open());
					}
				});
			}
			if (result.get() == 0) {
				List<Integer> secondaryIDList = new ArrayList<Integer>(PlotView.getOpenedPlotMap().values());
				for (int secondaryID : secondaryIDList) {
					PlotView oldView = (PlotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.findViewReference(PlotView.ID, String.valueOf(secondaryID)).getView(false);
					if (oldView != null) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(oldView);
					}

				}
			}
			return result.get() == 0 ? true : false;
		}

		@Override
		public void postShutdown(IWorkbench workbench) {
		}
	};

	private final IExecutionListener commandExecutionListener = new IExecutionListener() {
		@Override
		public void preExecute(String commandId, ExecutionEvent event) {
			if (commandId.equals("org.eclipse.ui.file.save"))
				SerializationConfigView.onModelSave();
		}

		@Override
		public void notHandled(String commandId, NotHandledException exception) {
		}

		@Override
		public void postExecuteFailure(String commandId, ExecutionException exception) {
		}

		@Override
		public void postExecuteSuccess(String commandId, Object returnValue) {
		}
	};

	private final IPartListener2 partListener = new IPartListener2() {
		private final String executionCommandId = "ru.bmstu.rk9.rao.ui.runtime.execute";
		private final String buildCommandId = "ru.bmstu.rk9.rao.ui.runtime.build";

		private final void updateExecutionContributions(IWorkbenchPartReference partRef) {
			ICommandService commandService = partRef.getPage().getWorkbenchWindow().getService(ICommandService.class);
			commandService.refreshElements(executionCommandId, null);
			commandService.refreshElements(buildCommandId, null);
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			updateExecutionContributions(partRef);
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			updateExecutionContributions(partRef);
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			updateExecutionContributions(partRef);
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			updateExecutionContributions(partRef);
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}
	};
}
