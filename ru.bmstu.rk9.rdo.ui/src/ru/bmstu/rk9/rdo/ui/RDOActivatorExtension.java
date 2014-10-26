package ru.bmstu.rk9.rdo.ui;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;

import ru.bmstu.rk9.rdo.ui.contributions.RDOSpeedSelectionToolbar;
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
