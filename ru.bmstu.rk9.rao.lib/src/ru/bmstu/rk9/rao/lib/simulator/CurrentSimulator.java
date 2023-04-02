package ru.bmstu.rk9.rao.lib.simulator;

import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.SystemEntryType;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class CurrentSimulator {
	private static ISimulator currentSimulator = null;

	public static void set(ISimulator simulator) {
		setCurrentSimulatorState(SimulatorState.DEINITIALIZED);
		currentSimulator = simulator;
	}

	public static synchronized void preinitialize() {
		preinitialize(new SimulatorPreinitializationInfo());
	}

	public static synchronized void preinitialize(SimulatorPreinitializationInfo preinitializationInfo) {
		if (isRunning)
			throw new RaoLibException("Cannot start new simulation while previous one is still running");

		if (currentSimulator == null)
			throw new RaoLibException("Cannot start new simulation: current simulator is not set");

		if (currentSimulatorState != SimulatorState.DEINITIALIZED)
			throw new RaoLibException("Cannot start new simulation: simulator wasn't deinitialized");

		currentSimulator.preinitilize(preinitializationInfo);
		setCurrentSimulatorState(SimulatorState.PREINITIALIZED);
	}

	public static synchronized void initialize(SimulatorInitializationInfo initializationInfo) {
		if (currentSimulatorState != SimulatorState.PREINITIALIZED)
			throw new RaoLibException("Simulation wasn't correctly preinitialized");

		currentSimulator.initialize(initializationInfo);
		setCurrentSimulatorState(SimulatorState.INITIALIZED);
	}

	private static SimulatorState currentSimulatorState = SimulatorState.DEINITIALIZED;

	public enum SimulatorState {
		INITIALIZED, DEINITIALIZED, PREINITIALIZED
	};

	private static final void setCurrentSimulatorState(SimulatorState simulatorState) {
		if (simulatorState == CurrentSimulator.currentSimulatorState)
			return;

		CurrentSimulator.currentSimulatorState = simulatorState;
		simulatorStateNotifier.notifySubscribers(simulatorState);
	}

	public static final void notifyError() {
		onFinish(SystemEntryType.RUN_TIME_ERROR);
		setCurrentSimulatorState(SimulatorState.DEINITIALIZED);
	}

	private static final Notifier<SimulatorState> simulatorStateNotifier = new Notifier<SimulatorState>(
			SimulatorState.class);

	public static final Notifier<SimulatorState> getSimulatorStateNotifier() {
		return simulatorStateNotifier;
	}

	public static boolean isInitialized() {
		return currentSimulatorState == SimulatorState.INITIALIZED;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	public static Database getDatabase() {
		return currentSimulator.getDatabase();
	}

	public static StaticModelData getStaticModelData() {
		return currentSimulator.getStaticModelData();
	}

	public static ModelState getModelState() {
		return currentSimulator.getModelState();
	}

	public static void setModelState(ModelState modelState) {
		currentSimulator.setModelState(modelState);
	}

	public static double getTime() {
		return currentSimulator.getTime();
	}

	public static void pushEvent(Event event) {
		currentSimulator.pushEvent(event);
	}

	public static Logger getLogger() {
		return currentSimulator.getLogger();
	}

	public enum ExecutionState {
		EXECUTION_STARTED, EXECUTION_COMPLETED, EXECUTION_ABORTED, STATE_CHANGED, TIME_CHANGED, SEARCH_STEP
	};

	public static Notifier<ExecutionState> getExecutionStateNotifier() {
		return currentSimulator.getExecutionStateNotifier();
	}

	private static void notifyChange(ExecutionState category) {
		currentSimulator.notifyChange(category);
	}

	public static List<AbstractResult<?>> getResults() {
		return currentSimulator.getResults();
	}

	private static volatile boolean isRunning = false;

	public static synchronized void stopExecution() {
		if (currentSimulatorState != SimulatorState.INITIALIZED)
			return;

		currentSimulator.abortExecution();
		notifyChange(ExecutionState.EXECUTION_ABORTED);
	}

	public enum SimulationStopCode {
		USER_INTERRUPT, NO_MORE_EVENTS, TERMINATE_CONDITION, RUNTIME_ERROR, SIMULATION_CONTINUES
	}

	public static void runExperiments() {
		currentSimulator.runExperiments();
	}

	public static SimulationStopCode run() {
		isRunning = true;

		return stop(currentSimulator.run());
	}

	private static void onFinish(Database.SystemEntryType simFinishType) {
		try {
			currentSimulator.getDatabase().addSystemEntry(simFinishType);
			notifyChange(ExecutionState.EXECUTION_COMPLETED);
		} finally {
			isRunning = false;
		}
	}

	private static SimulationStopCode stop(SimulationStopCode code) {
		Database.SystemEntryType simFinishType;
		switch (code) {
		case USER_INTERRUPT:
			simFinishType = SystemEntryType.ABORT;
			break;
		case NO_MORE_EVENTS:
			simFinishType = SystemEntryType.NO_MORE_EVENTS;
			break;
		case TERMINATE_CONDITION:
			simFinishType = SystemEntryType.NORMAL_TERMINATION;
			break;
		case RUNTIME_ERROR:
			simFinishType = SystemEntryType.RUN_TIME_ERROR;
			break;
		default:
			throw new RaoLibException("Internal error: invalid simulation stop code");
		}

		onFinish(simFinishType);
		return code;
	}
}
