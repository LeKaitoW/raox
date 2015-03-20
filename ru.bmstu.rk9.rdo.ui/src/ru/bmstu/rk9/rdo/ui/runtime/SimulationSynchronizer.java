package ru.bmstu.rk9.rdo.ui.runtime;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rdo.lib.DecisionPointSearch;
import ru.bmstu.rk9.rdo.lib.Simulator;
import ru.bmstu.rk9.rdo.lib.Subscriber;
import ru.bmstu.rk9.rdo.ui.contributions.RDOSpeedSelectionToolbar;
import ru.bmstu.rk9.rdo.ui.contributions.RDOStatusView;

public class SimulationSynchronizer {
	public static enum ExecutionMode {
		NO_ANIMATION, FAST_FORWARD, NORMAL_SPEED, PAUSE
	}

	private static SimulationSynchronizer INSTANCE;

	public static SimulationSynchronizer getInstance() {
		return INSTANCE;
	}

	private SimulationSynchronizer() {
		INSTANCE = this;

		setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
		setSimulationSpeed(RDOSpeedSelectionToolbar.getSpeed());
	}

	public static void start() {
		new SimulationSynchronizer();
	}

	public static void finish() {
		INSTANCE = null;
	}

	private volatile ExecutionMode executionMode = null;

	public static void setState(String state) {
		if (INSTANCE == null)
			return;

		switch (state) {
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

	public class UITimeUpdater implements Subscriber {
		private long lastUpdateTime = System.currentTimeMillis();
		private double actualTimeScale = 0;

		private Display display = PlatformUI.getWorkbench().getDisplay();

		private DecimalFormat scaleFormatter = new DecimalFormat("0.######");
		private DecimalFormat timeFormatter = new DecimalFormat("0.0#####");

		Runnable updater = () -> {
			RDOStatusView.setValue("Simulation time".intern(), 20,
					timeFormatter.format(Simulator.getTime()));
			RDOStatusView.setValue("Actual scale".intern(), 10,
					scaleFormatter.format(60060d / actualTimeScale));
		};

		@Override
		public void fireChange() {
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastUpdateTime > 50) {
				lastUpdateTime = currentTime;
				if (!display.isDisposed())
					display.asyncExec(updater);
			}
		}
	}

	private volatile boolean simulationAborted = false;

	public class SimulationStateListener implements Subscriber {
		@Override
		public void fireChange() {
			SimulationSynchronizer.this.simulationAborted = true;
		}
	}

	public static void setSimulationSpeed(int value) {
		if (INSTANCE == null)
			return;

		if (value < 1 || value > 100)
			return;

		INSTANCE.simulationManager.speedDelayMillis = (long) (-Math
				.log10(value / 100d) * 1000d);
		DecisionPointSearch.delay = (int)INSTANCE.simulationManager.speedDelayMillis;
	}

	public static void setSimulationScale(double value) {
		if (INSTANCE == null)
			return;

		INSTANCE.simulationManager.timeScale = 60060d / value;
		INSTANCE.simulationManager.startRealTime = System.currentTimeMillis();
		INSTANCE.simulationManager.startSimulationTime =
				Simulator.isRunning() ? Simulator.getTime() : 0;
	}

	public class SimulationManager {
		private volatile double timeScale = 0.3;

		private long startRealTime;
		private double startSimulationTime;

		public class ScaleManager implements Subscriber {
			@Override
			public void fireChange() {
				double currentSimulationTime = Simulator.getTime();
				long currentRealTime = System.currentTimeMillis();

				if (currentSimulationTime != 0) {
					switch (executionMode) {
					case PAUSE:

						processPause();

						break;

					case NORMAL_SPEED:

						long waitTime = (long) ((currentSimulationTime - startSimulationTime) * timeScale)
								- (currentRealTime - startRealTime);

						if (waitTime > 0) {
							while (executionMode == ExecutionMode.NORMAL_SPEED
									&& waitTime > 0 && !simulationAborted) {
								delay(waitTime > 50 ? 50 : waitTime);
								waitTime = (long) ((currentSimulationTime - startSimulationTime) * timeScale)
										- (System.currentTimeMillis() - startRealTime);
							}
							uiTimeUpdater.actualTimeScale = timeScale;
						} else
							uiTimeUpdater.actualTimeScale = ((double) (currentRealTime - startRealTime))
									/ currentSimulationTime;

						break;

					default:
						uiTimeUpdater.actualTimeScale = 0;
						updateTimes();
					}
				}
			}
		}

		public final ScaleManager scaleManager = new ScaleManager();

		private volatile long speedDelayMillis = 0;

		public class SpeedManager implements Subscriber {
			@Override
			public void fireChange() {
				switch (executionMode) {
				case PAUSE:

					processPause();

					break;

				case FAST_FORWARD:
				case NORMAL_SPEED:

					long startTime = System.currentTimeMillis();

					long timeToWait = speedDelayMillis;
					while (timeToWait > 0
							&& (executionMode == ExecutionMode.FAST_FORWARD
								|| executionMode == ExecutionMode.NORMAL_SPEED)
									&& !simulationAborted) {
						delay(timeToWait > 50 ? 50 : timeToWait);
						timeToWait = startTime + speedDelayMillis
								- System.currentTimeMillis();
					}

					break;

				default:
					// Do nothing
				}
			}
		}

		public final SpeedManager speedManager = new SpeedManager();

		private void processPause() {
			while (executionMode == ExecutionMode.PAUSE && !simulationAborted)
				delay(25);

			updateTimes();
		}

		private void updateTimes()
		{
			startRealTime = System.currentTimeMillis();
			startSimulationTime = Simulator.getTime();
		}
	}

	private static void delay(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public final UITimeUpdater uiTimeUpdater = new UITimeUpdater();
	public final SimulationManager simulationManager = new SimulationManager();
	public final SimulationStateListener simulationStateListener = new SimulationStateListener();
}
