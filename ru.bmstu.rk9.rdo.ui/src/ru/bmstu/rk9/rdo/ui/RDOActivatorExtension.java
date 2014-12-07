package ru.bmstu.rk9.rdo.ui;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.osgi.framework.BundleContext;

import ru.bmstu.rk9.rdo.ui.contributions.RDOSpeedSelectionToolbar;
import ru.bmstu.rk9.rdo.ui.contributions.RDOTraceConfigView;
import ru.bmstu.rk9.rdo.ui.internal.RDOActivator;
import ru.bmstu.rk9.rdo.ui.runtime.SetSimulationScaleHandler;

public class RDOActivatorExtension extends RDOActivator
{
	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		IEclipsePreferences prefs =
			InstanceScope.INSTANCE.getNode("ru.bmstu.rk9.rdo.ui");

		RDOSpeedSelectionToolbar.setSpeed(prefs.getInt("SimulationSpeed", 100));
		SetSimulationScaleHandler.setSimulationScale(prefs.getDouble("SimulationScale", 3600d));

		ICommandService commandService = (ICommandService) PlatformUI
			.getWorkbench().getService(ICommandService.class);
		commandService.addExecutionListener(
			new IExecutionListener()
			{
				@Override
				public void preExecute(String commandId, ExecutionEvent event)
				{
					if (commandId.equals("org.eclipse.ui.file.save"))
						RDOTraceConfigView.onModelSave();
				}

				@Override
				public void notHandled(
					String commandId, NotHandledException exception) {}

				@Override
				public void postExecuteFailure(
					String commandId, ExecutionException exception) {}

				@Override
				public void postExecuteSuccess(
					String commandId,Object returnValue){}
			}
		);
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		IEclipsePreferences prefs =
				InstanceScope.INSTANCE.getNode("ru.bmstu.rk9.rdo.ui");

		prefs.putInt("SimulationSpeed", RDOSpeedSelectionToolbar.getSpeed());
		prefs.putDouble("SimulationScale", SetSimulationScaleHandler.getSimulationScale());

		super.stop(context);
	}
}
