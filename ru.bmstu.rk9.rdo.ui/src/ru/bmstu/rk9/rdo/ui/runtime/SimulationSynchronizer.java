package ru.bmstu.rk9.rdo.ui.runtime;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;

import org.eclipse.ui.IEditorSite;
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

		Runnable updater = new Runnable()
		{
			@Override
			public void run()
			{
				RDOStatusView.setSimulationTime(Simulator.getTime());
			}
		};

		@Override
		public void fireChange()
		{
			long currentTime = System.currentTimeMillis();
			if(currentTime - lastUpdateTime > 50)
			{
				lastUpdateTime = currentTime;
				PlatformUI.getWorkbench().getDisplay().asyncExec(updater);
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

		// this variable is needed in order to prevent SimulationScaleManager from trying
		// to compensate delays caused by SimulationSpeedManager. SimulationScaleManager
		// will subtract this variable from realTimeDelta and won't try to "catch up"
		private long accumulatedDelay;

		@Override
		public void fireChange()
		{
			switch(executionMode)
			{
				case PAUSE:

					while(executionMode == ExecutionMode.PAUSE && !simulationAborted)
						delay(50);
	
					simulationScaleManager.lastRealTime = System.currentTimeMillis();
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

					accumulatedDelay += System.currentTimeMillis() - startTime;
					break;

				default:
					// Do nothing
			}
		}
	}

	public static void setSimulationScale(double value)
	{
		if(INSTANCE == null)
			return;

		INSTANCE.simulationScaleManager.timeScale = 60600d / value;
	}

	public class SimulationScaleManager implements Subscriber
	{
		private volatile double timeScale = 0.3;

		private double lastSimulationTime;

		private long lastRealTime;

		private long totalTimeLag = 0;

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

						double simulationDelta = currentSimulationTime - lastSimulationTime;
						long realTimeDelta = currentRealTime - (lastRealTime + simulationSpeedManager.accumulatedDelay);
						long waitTime = (long)(simulationDelta * timeScale) - realTimeDelta;

						if(waitTime > 0)
						{
							long maxCatchUp = waitTime / 2;
							long timeToCatchUp = totalTimeLag > maxCatchUp ? maxCatchUp : totalTimeLag;
							totalTimeLag -= timeToCatchUp;
							
							long leftToSleep = waitTime - timeToCatchUp;
							while(executionMode == ExecutionMode.NORMAL_SPEED && leftToSleep > 0 && !simulationAborted)
							{
								delay(leftToSleep > 50 ? 50 : leftToSleep);
								leftToSleep = (long)(simulationDelta * timeScale) + (lastRealTime +
									simulationSpeedManager.accumulatedDelay) - System.currentTimeMillis() - timeToCatchUp;
							}
							uiTimeUpdater.actualTimeScale = 0;
						}
						else
						{
							totalTimeLag -= waitTime;
							uiTimeUpdater.actualTimeScale = ((double)realTimeDelta)/simulationDelta;
						}
						break;

					default:
						// Do nothing
				}
			}

			simulationSpeedManager.accumulatedDelay = 0;

			lastSimulationTime = currentSimulationTime;
			lastRealTime = System.currentTimeMillis();
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
