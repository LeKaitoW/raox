package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;

import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.commands.ICommandService;

import ru.bmstu.rk9.rdo.lib.Simulator;

import ru.bmstu.rk9.rdo.lib.Subscriber;

import ru.bmstu.rk9.rdo.ui.contributions.RDOSpeedSelectionToolbar;

import ru.bmstu.rk9.rdo.ui.contributions.RDOStatusView;

public class SimulationSynchronizer
{
	public static enum ExecutionMode
	{
		NO_ANIMATION, FAST_FORWARD, NORMAL_SPEED, PAUSE
	}

	private static SimulationSynchronizer INSTANCE;

	public static SimulationSynchronizer getInstance()
	{
		return INSTANCE;
	}

	private SimulationSynchronizer()
	{
		INSTANCE = this;

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = service.getCommand("ru.bmstu.rk9.rdo.ui.runtime.setExecutionMode");
		State state = command.getState("org.eclipse.ui.commands.radioState");

		setState((String)state.getValue());

		setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
		setSimulationSpeed(RDOSpeedSelectionToolbar.getSpeed());
	}

	public static void start()
	{
		new SimulationSynchronizer();
	}

	public static void finish()
	{
		INSTANCE = null;
	}

	private volatile ExecutionMode executionMode = null;

	public static void setState(String state)
	{
		if(INSTANCE == null)
			return;

		switch (state)
		{
			case "NA":
				INSTANCE.executionMode = ExecutionMode.NO_ANIMATION;
			break;
			case "FF":
				INSTANCE.executionMode = ExecutionMode.FAST_FORWARD;
			break;
			case "NS":
				INSTANCE.executionMode = ExecutionMode.NORMAL_SPEED;
			break;
			case "P":
				INSTANCE.executionMode = ExecutionMode.PAUSE;
			break;
		}
	}

	public class UITimeUpdater implements Subscriber
	{
		private long lastUpdateTime = System.currentTimeMillis();
		private double actualTimeScale = 0;

		private Display display = PlatformUI.getWorkbench().getDisplay();

		Runnable updater = () ->
		{
			RDOStatusView.setSimulationTime(Simulator.getTime());
			RDOStatusView.setActualSimulationScale(60060d / actualTimeScale);
		};

		@Override
		public void fireChange()
		{
			long currentTime = System.currentTimeMillis();
			if(currentTime - lastUpdateTime > 50)
			{
				lastUpdateTime = currentTime;
				if(!display.isDisposed())
					display.asyncExec(updater);
			}
		}
	}

	private volatile boolean simulationAborted = false;

	public class SimulationStateListener implements Subscriber
	{
		@Override
		public void fireChange()
		{
			SimulationSynchronizer.this.simulationAborted = true;
		}
	}

	public static void setSimulationSpeed(int value)
	{
		if(INSTANCE == null)
			return;

		if(value < 1 || value > 100)
			return;

		INSTANCE.simulationSpeedManager.speedDelayMillis =
			(long)(-Math.log10(value/100d)*1000d);
	}

	public class SimulationSpeedManager implements Subscriber
	{
		private volatile long speedDelayMillis = 0;

		@Override
		public void fireChange()
		{
			notifyTracer(executionMode);
			switch(executionMode)
			{
				case PAUSE:

					while(executionMode == ExecutionMode.PAUSE && !simulationAborted)
						delay(50);

					break;

				case FAST_FORWARD:
				case NORMAL_SPEED:

					long startTime = System.currentTimeMillis();

					long timeToWait = speedDelayMillis;
					while(timeToWait > 0 && (executionMode == ExecutionMode.FAST_FORWARD ||
						executionMode == ExecutionMode.NORMAL_SPEED) && !simulationAborted)
					{
						delay(timeToWait > 50 ? 50 : timeToWait);
						timeToWait = startTime + speedDelayMillis - System.currentTimeMillis();
					}

					break;

				default:
					// Do nothing
			}
		}

		private final void notifyTracer(ExecutionMode mode)
		{
			switch(mode)
			{
			case PAUSE:
			case NO_ANIMATION:
				Simulator.getTracer().setPaused(true);
				break;
			case FAST_FORWARD:
			case NORMAL_SPEED:
				Simulator.getTracer().setPaused(false);
				break;
			}
		}
	}

	public static void setSimulationScale(double value)
	{
		if(INSTANCE == null)
			return;

		INSTANCE.simulationScaleManager.timeScale = 60060d / value;
		INSTANCE.simulationScaleManager.startRealTime = System.currentTimeMillis();
		INSTANCE.simulationScaleManager.startSimulationTime =
			Simulator.isRunning() ? Simulator.getTime() : 0;
	}

	public class SimulationScaleManager implements Subscriber
	{
		private volatile double timeScale = 0.3;

		private long startRealTime;
		private double startSimulationTime;

		@Override
		public void fireChange()
		{
			double currentSimulationTime = Simulator.getTime();
			long currentRealTime = System.currentTimeMillis();

			if(currentSimulationTime != 0)
			{
				switch(executionMode)
				{
					case PAUSE:

						while(executionMode == ExecutionMode.PAUSE && !simulationAborted)
							delay(50);

						currentRealTime = System.currentTimeMillis();
						break;

					case NORMAL_SPEED:

						long waitTime = (long)((currentSimulationTime - startSimulationTime) * timeScale) -
							(currentRealTime - startRealTime);

						if(waitTime > 0)
						{
							while(executionMode == ExecutionMode.NORMAL_SPEED && waitTime > 0 && !simulationAborted)
							{
								delay(waitTime > 50 ? 50 : waitTime);
								waitTime = (long)((currentSimulationTime - startSimulationTime) * timeScale) -
									(System.currentTimeMillis() - startRealTime);
							}
							uiTimeUpdater.actualTimeScale = timeScale;
						}
						else
							uiTimeUpdater.actualTimeScale = ((double)(currentRealTime - startRealTime))/currentSimulationTime;

						break;

					default:
						// Do nothing
				}
			}
		}
	}

	private static void delay(long milliseconds)
	{
		try
		{
			Thread.sleep(milliseconds);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public final UITimeUpdater uiTimeUpdater = new UITimeUpdater();
	public final SimulationScaleManager simulationScaleManager = new SimulationScaleManager();
	public final SimulationSpeedManager simulationSpeedManager = new SimulationSpeedManager();
	public final SimulationStateListener simulationStateListener = new SimulationStateListener();
}
