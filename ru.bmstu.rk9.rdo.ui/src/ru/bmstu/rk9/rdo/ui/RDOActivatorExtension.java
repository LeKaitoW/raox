package ru.bmstu.rk9.rdo.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.osgi.framework.BundleContext;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.ISourceProviderService;

import ru.bmstu.rk9.rdo.ui.internal.RDOActivator;
import ru.bmstu.rk9.rdo.ui.contributions.PlotView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOResultsView;
import ru.bmstu.rk9.rdo.ui.contributions.RDOSpeedSelectionToolbar;
import ru.bmstu.rk9.rdo.ui.contributions.RDOSerializationConfigView;
import ru.bmstu.rk9.rdo.ui.animation.RDOAnimationView;
import ru.bmstu.rk9.rdo.ui.runtime.ModelExecutionSourceProvider;
import ru.bmstu.rk9.rdo.ui.runtime.SetSimulationScaleHandler;

public class RDOActivatorExtension extends RDOActivator {
	private MessageDialog closeDialog = new MessageDialog(Display.getDefault()
			.getActiveShell(), "Error", null,
			"Simulation should be stopped prior to shutdown!",
			MessageDialog.ERROR, new String[] { "Force Shutdown", "OK" }, 1);

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode("ru.bmstu.rk9.rdo.ui");

		RDOSpeedSelectionToolbar.setSpeed(prefs.getInt("SimulationSpeed", 100));
		SetSimulationScaleHandler.setSimulationScale(prefs.getDouble(
				"SimulationScale", 3600d));

		IWorkbench workbench = PlatformUI.getWorkbench();

		ICommandService commandService = (ICommandService) workbench
				.getService(ICommandService.class);
		commandService.addExecutionListener(new IExecutionListener() {
			@Override
			public void preExecute(String commandId, ExecutionEvent event) {
				if (commandId.equals("org.eclipse.ui.file.save"))
					RDOSerializationConfigView.onModelSave();
			}

			@Override
			public void notHandled(String commandId,
					NotHandledException exception) {
			}

			@Override
			public void postExecuteFailure(String commandId,
					ExecutionException exception) {
			}

			@Override
			public void postExecuteSuccess(String commandId, Object returnValue) {
			}
		});

		workbench.addWorkbenchListener(new IWorkbenchListener() {
			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				ISourceProviderService sourceProviderService = (ISourceProviderService) workbench
						.getActiveWorkbenchWindow().getService(
								ISourceProviderService.class);
				ModelExecutionSourceProvider sourceProvider = (ModelExecutionSourceProvider) sourceProviderService
						.getSourceProvider(ModelExecutionSourceProvider.ModelExecutionKey);

				final AtomicInteger result = new AtomicInteger(0);
				if (sourceProvider.getCurrentState().get(
						ModelExecutionSourceProvider.ModelExecutionKey) == ModelExecutionSourceProvider.running) {
					workbench.getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							result.set(closeDialog.open());
						}
					});
				}
				if (result.get() == 0) {
					List<Integer> secondaryIDList = new ArrayList<Integer>(
							PlotView.getOpenedPlotMap().values());
					for (int secondaryID : secondaryIDList) {
						PlotView oldView = (PlotView) PlatformUI
								.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.findViewReference(PlotView.ID,
										String.valueOf(secondaryID))
								.getView(false);
						if (oldView != null) {
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.hideView(oldView);
						}

					}
				}
				return result.get() == 0 ? true : false;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		IEclipsePreferences prefs = InstanceScope.INSTANCE
				.getNode("ru.bmstu.rk9.rdo.ui");

		prefs.putInt("SimulationSpeed", RDOSpeedSelectionToolbar.getSpeed());
		prefs.putDouble("SimulationScale",
				SetSimulationScaleHandler.getSimulationScale());

		int animationFrameListSize = RDOAnimationView.getFrameListSize();
		if (animationFrameListSize != SWT.DEFAULT)
			prefs.putInt("AnimationViewFrameListSize", animationFrameListSize);

		RDOResultsView.savePreferences();

		super.stop(context);
	}
}
