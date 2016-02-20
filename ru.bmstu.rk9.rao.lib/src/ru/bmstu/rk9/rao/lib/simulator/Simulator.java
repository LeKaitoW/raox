package ru.bmstu.rk9.rao.lib.simulator;

import java.util.LinkedList;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.SystemEntryType;
import ru.bmstu.rk9.rao.lib.dpt.DPTManager;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.event.EventScheduler;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.modelStructure.ModelStructureCache;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.result.ResultManager;

public class Simulator {
	private static Simulator INSTANCE = null;

	public static synchronized void preinitialize(SimulatorPreinitializationInfo preinitializationInfo) {
		if (isRunning)
			throw new RaoLibException("Cannot start new simulation while previous one is still running");

		setSimulationState(SimulatorState.DEINITIALIZED);
		INSTANCE = new Simulator();

		INSTANCE.modelState = new ModelState(preinitializationInfo.resourceClasses);
		INSTANCE.database = new Database(preinitializationInfo.modelStructure);
		INSTANCE.modelStructureCache = new ModelStructureCache();

		setSimulationState(SimulatorState.PREINITIALIZED);
	}

	public static synchronized void initSimulation(SimulatorInitializationInfo initializationInfo) {
		if (simulatorState != SimulatorState.PREINITIALIZED)
			throw new RaoLibException("Simulation wasn't correctly preinitialized");

		INSTANCE.executionStateNotifier = new Notifier<ExecutionState>(ExecutionState.class);
		INSTANCE.dptManager = new DPTManager(initializationInfo.decisionPoints);

		for (Supplier<Boolean> terminateCondition : initializationInfo.terminateConditions)
			Simulator.addTerminateCondition(terminateCondition);

		for (Runnable init : initializationInfo.initList)
			init.run();

		setSimulationState(SimulatorState.INITIALIZED);
	}

	private static SimulatorState simulatorState = SimulatorState.DEINITIALIZED;

	public enum SimulatorState {
		INITIALIZED, DEINITIALIZED, PREINITIALIZED
	};

	private static final void setSimulationState(SimulatorState simulatorState) {
		if (simulatorState == Simulator.simulatorState)
			return;

		Simulator.simulatorState = simulatorState;
		simulatorStateNotifier.notifySubscribers(simulatorState);
	}

	public static final void notifyError() {
		onFinish(SystemEntryType.RUN_TIME_ERROR);
		setSimulationState(SimulatorState.DEINITIALIZED);
	}

	private static final Notifier<SimulatorState> simulatorStateNotifier = new Notifier<SimulatorState>(
			SimulatorState.class);

	public static final Notifier<SimulatorState> getSimulatorStateNotifier() {
		return simulatorStateNotifier;
	}

	public static boolean isInitialized() {
		return simulatorState == SimulatorState.INITIALIZED;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private Database database;

	public static Database getDatabase() {
		return INSTANCE.database;
	}

	private ModelState modelState;

	public static ModelState getModelState() {
		return INSTANCE.modelState;
	}

	private ModelStructureCache modelStructureCache;

	public static ModelStructureCache getModelStructureCache() {
		return INSTANCE.modelStructureCache;
	}

	private volatile double time = 0;

	public static double getTime() {
		return INSTANCE.time;
	}

	private EventScheduler eventScheduler = new EventScheduler();

	public static void pushEvent(Event event) {
		INSTANCE.eventScheduler.pushEvent(event);
	}

	private LinkedList<Supplier<Boolean>> terminateList = new LinkedList<>();

	private static void addTerminateCondition(Supplier<Boolean> terminateCondition) {
		INSTANCE.terminateList.add(terminateCondition);
	}

	private DPTManager dptManager;

	private ResultManager resultManager = new ResultManager();

	public static void addResult(Result result) {
		INSTANCE.resultManager.addResult(result);
	}

	public static LinkedList<Result> getResults() {
		return INSTANCE.resultManager.getResults();
	}

	public enum ExecutionState {
		EXECUTION_STARTED, EXECUTION_COMPLETED, EXECUTION_ABORTED, STATE_CHANGED, TIME_CHANGED, SEARCH_STEP
	};

	private Notifier<ExecutionState> executionStateNotifier;

	public static Notifier<ExecutionState> getExecutionStateNotifier() {
		return INSTANCE.executionStateNotifier;
	}

	private static void notifyChange(ExecutionState category) {
		INSTANCE.executionStateNotifier.notifySubscribers(category);
	}

	private boolean checkTerminate() {
		for (Supplier<Boolean> c : terminateList)
			if (c.get())
				return true;
		return false;
	}

	private static volatile boolean isRunning = false;

	private volatile boolean executionAborted = false;

	public static synchronized void stopExecution() {
		if (simulatorState != SimulatorState.INITIALIZED)
			return;

		INSTANCE.executionAborted = true;
		notifyChange(ExecutionState.EXECUTION_ABORTED);
	}

	public enum SimulationStopCode {
		USER_INTERRUPT, NO_MORE_EVENTS, TERMINATE_CONDITION, RUNTIME_ERROR, SIMULATION_CONTINUES
	}

	public static SimulationStopCode run() {
		isRunning = true;

		INSTANCE.database.addSystemEntry(Database.SystemEntryType.SIM_START);

		notifyChange(ExecutionState.EXECUTION_STARTED);
		notifyChange(ExecutionState.TIME_CHANGED);
		notifyChange(ExecutionState.STATE_CHANGED);

		while (!INSTANCE.executionAborted) {
			if (INSTANCE.checkTerminate())
				return stop(SimulationStopCode.TERMINATE_CONDITION);

			if (INSTANCE.dptManager.checkDPT()) {
				notifyChange(ExecutionState.STATE_CHANGED);
				continue;
			}

			if (!INSTANCE.eventScheduler.haveEvents())
				return stop(SimulationStopCode.NO_MORE_EVENTS);

			Event event = INSTANCE.eventScheduler.popEvent();
			INSTANCE.time = event.getTime();
			event.run();

			notifyChange(ExecutionState.TIME_CHANGED);
			notifyChange(ExecutionState.STATE_CHANGED);
		}

		return stop(SimulationStopCode.USER_INTERRUPT);
	}

	private static void onFinish(Database.SystemEntryType simFinishType) {
		try {
			INSTANCE.database.addSystemEntry(simFinishType);
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
