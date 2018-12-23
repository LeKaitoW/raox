package ru.bmstu.rk9.rao.ui.simulation;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.notification.RealTimeSubscriberManager;

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
			throw new SimulationComponentsException("Unknown simulation mode: " + type);
		}

		public String getString() {
			return type;
		}

		final private String type;
	}

	public SimulationSynchronizer() {
		initializeSubscribers();
	}

	private final void initializeSubscribers() {
		simulationSubscriberManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(simulationManager.scaleManager, ExecutionState.TIME_CHANGED),
						new SimulatorSubscriberInfo(simulationManager.speedManager, ExecutionState.STATE_CHANGED),
						new SimulatorSubscriberInfo(simulationManager.speedManager, ExecutionState.SEARCH_STEP),
						new SimulatorSubscriberInfo(executionAbortedListener, ExecutionState.EXECUTION_ABORTED),
						new SimulatorSubscriberInfo(executionStartedListener, ExecutionState.EXECUTION_STARTED)));
	}

	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
		uiTimeUpdater.deinitializeSubscribers();
	}

	private volatile ExecutionMode executionMode;

	public void setExecutionMode(ExecutionMode executionMode) {
		this.executionMode = executionMode;
	}

	public class UITimeUpdater {
		UITimeUpdater() {
			initializeSubscribers();
		}

		private final void initializeSubscribers() {
			simulatorSubscriberManager.initialize(
					Arrays.asList(new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_STARTED),
							new SimulatorSubscriberInfo(commonSubscriber, ExecutionState.EXECUTION_COMPLETED)));
			realTimeSubscriberManager.initialize(Arrays.asList(updater));
		}

		private final void deinitializeSubscribers() {
			simulatorSubscriberManager.deinitialize();
			realTimeSubscriberManager.deinitialize();
		}

		private double actualTimeScale = 0;

		private DecimalFormat scaleFormatter = new DecimalFormat("0.######");
		private DecimalFormat defaultTimeFormatter = new DecimalFormat("0.0#####");
		private Function<Double, String> defaultTimeFormatterFunction = defaultTimeFormatter::format;

		private final SimulatorSubscriberManager simulatorSubscriberManager = new SimulatorSubscriberManager();
		private final RealTimeSubscriberManager realTimeSubscriberManager = new RealTimeSubscriberManager();

		private Runnable updater = () -> {
			String simulationTime = CurrentSimulator.formatTime(CurrentSimulator.getTime(),
					defaultTimeFormatterFunction);
			StatusView.setValue("Simulation time".intern(), 20, simulationTime);
			StatusView.setValue("Actual scale".intern(), 10, scaleFormatter.format(60060d / actualTimeScale));
		};

		Subscriber commonSubscriber = new Subscriber() {
			@Override
			public void fireChange() {
				PlatformUI.getWorkbench().getDisplay().asyncExec(updater);
			}
		};
	}

	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();

	private volatile boolean simulationAborted = false;

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
			setSimulationScale(SetSimulationScaleHandler.getSimulationScale());
			setSimulationSpeed(SpeedSelectionToolbar.getSpeed());
			setExecutionMode(SimulationModeDispatcher.getMode());
		}
	}

	public void setSimulationSpeed(int value) {
		if (value < 1 || value > 100)
			throw new SimulationComponentsException("Incorrect simulation speed value " + value);

		simulationManager.speedDelayMillis = (long) (-Math.log10(value / 100d) * 1000d);
	}

	public void setSimulationScale(double value) {
		simulationManager.timeScale = 60060d / value;
		simulationManager.startRealTime = System.currentTimeMillis();
		simulationManager.startSimulationTime = CurrentSimulator.isRunning() ? CurrentSimulator.getTime() : 0;
	}

	public class SimulationManager {
		private volatile double timeScale = 0.3;

		private long startRealTime;
		private double startSimulationTime;

		public class ScaleManager implements Subscriber {
			@Override
			public void fireChange() {
				double currentSimulationTime = CurrentSimulator.getTime();
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
							while (executionMode == ExecutionMode.NORMAL_SPEED && waitTime > 0 && !simulationAborted) {
								delay(waitTime > 50 ? 50 : waitTime);
								waitTime = (long) ((currentSimulationTime - startSimulationTime) * timeScale)
										- (System.currentTimeMillis() - startRealTime);
							}
							uiTimeUpdater.actualTimeScale = timeScale;
						} else
							uiTimeUpdater.actualTimeScale = (currentRealTime - startRealTime) / currentSimulationTime;
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
					while (timeToWait > 0 && (executionMode == ExecutionMode.FAST_FORWARD
							|| executionMode == ExecutionMode.NORMAL_SPEED) && !simulationAborted) {
						delay(timeToWait > 50 ? 50 : timeToWait);
						timeToWait = startTime + speedDelayMillis - System.currentTimeMillis();
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
			startSimulationTime = CurrentSimulator.getTime();
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
