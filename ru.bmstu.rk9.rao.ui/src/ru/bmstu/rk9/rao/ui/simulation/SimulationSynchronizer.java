package ru.bmstu.rk9.rao.ui.simulation;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.ui.notification.SubscriberRegistrationManager;

public class SimulationSynchronizer {
	public static enum ExecutionMode {
		PAUSE("P"), NO_ANIMATION("NA"), FAST_FORWARD("FF"), NORMAL_SPEED("NS");

		ExecutionMode(final String type) {
			this.type = type;
		}

		public static final ExecutionMode getByString(final String type) {
			for (final ExecutionMode executionMode : values()) {
				if (executionMode.type.equals(type))
					return executionMode;
			}
			throw new SimulationComponentsException("Unknown simulation mode: "
					+ type);
		}

		public String getString() {
			return type;
		}

		final private String type;
	}

	public SimulationSynchronizer() {
		setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
		setSimulationSpeed(SpeedSelectionToolbar.getSpeed());
		setExecutionMode(SimulationModeDispatcher.getMode());
		simulationAborted = false;
		initialize();
	}

	private final void initialize() {
		subscriberRegistrationManager
				.enlistCommonSubscriber(uiTimeUpdater,
						ExecutionState.TIME_CHANGED)
				.enlistCommonSubscriber(simulationManager.scaleManager,
						ExecutionState.TIME_CHANGED)
				.enlistCommonSubscriber(simulationManager.speedManager,
						ExecutionState.STATE_CHANGED)
				.enlistCommonSubscriber(simulationManager.speedManager,
						ExecutionState.SEARCH_STEP)
				.enlistCommonSubscriber(executionAbortedListener,
						ExecutionState.EXECUTION_ABORTED)
				.enlistCommonSubscriber(executionStartedListener,
						ExecutionState.EXECUTION_STARTED);
		subscriberRegistrationManager.initialize();
	}

	public final void deinitialize() {
		subscriberRegistrationManager.deinitialize();
	}

	private volatile ExecutionMode executionMode;

	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	public class UITimeUpdater implements Subscriber {
		private long lastUpdateTime = System.currentTimeMillis();
		private double actualTimeScale = 0;

		private Display display = PlatformUI.getWorkbench().getDisplay();

		private DecimalFormat scaleFormatter = new DecimalFormat("0.######");
		private DecimalFormat timeFormatter = new DecimalFormat("0.0#####");

		private Runnable updater = () -> {
			StatusView.setValue("Simulation time".intern(), 20,
					timeFormatter.format(Simulator.getTime()));
			StatusView.setValue("Actual scale".intern(), 10,
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

	private final SubscriberRegistrationManager subscriberRegistrationManager = new SubscriberRegistrationManager();

	private volatile boolean simulationAborted;

	public class ExecutionAbortedListener implements Subscriber {
		@Override
		public void fireChange() {
			SimulationSynchronizer.this.simulationAborted = true;
		}
	}

	public class ExecutionStartedListener implements Subscriber {
		@Override
		public void fireChange() {
			SimulationSynchronizer.this.simulationAborted = false;
		}
	}

	public void setSimulationSpeed(int value) {
		if (value < 1 || value > 100)
			return;

		simulationManager.speedDelayMillis = (long) (-Math.log10(value / 100d) * 1000d);
	}

	public void setSimulationScale(double value) {
		simulationManager.timeScale = 60060d / value;
		simulationManager.startRealTime = System.currentTimeMillis();
		simulationManager.startSimulationTime = Simulator.isRunning() ? Simulator
				.getTime() : 0;
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
							&& (executionMode == ExecutionMode.FAST_FORWARD || executionMode == ExecutionMode.NORMAL_SPEED)
							&& !simulationAborted) {
						delay(timeToWait > 50 ? 50 : timeToWait);
						timeToWait = startTime + speedDelayMillis
								- System.currentTimeMillis();
					}

					break;
				default:
					break;
				}
			}
		}

		public final SpeedManager speedManager = new SpeedManager();

		private void processPause() {
			while (executionMode == ExecutionMode.PAUSE && !simulationAborted)
				delay(25);

			updateTimes();
		}

		private void updateTimes() {
			startRealTime = System.currentTimeMillis();
			startSimulationTime = Simulator.getTime();
		}
	}

	private void delay(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public final UITimeUpdater uiTimeUpdater = new UITimeUpdater();
	public final SimulationManager simulationManager = new SimulationManager();
	public final ExecutionAbortedListener executionAbortedListener = new ExecutionAbortedListener();
	public final ExecutionStartedListener executionStartedListener = new ExecutionStartedListener();
}
