package ru.bmstu.rk9.rao.lib.simulator;

import java.util.LinkedList;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.database.Database.SystemEntryType;
import ru.bmstu.rk9.rao.lib.dpt.DPTManager;
import ru.bmstu.rk9.rao.lib.dpt.DecisionPoint;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.event.EventScheduler;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modelStructure.ModelStructureCache;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.result.ResultManager;

public class Simulator {
	private static Simulator INSTANCE = null;

	public static synchronized void initSimulation(JSONObject modelStructure) {
		if (isRunning)
			throw new SimulatorException("Simulation is already initialized");

		setSimulationState(SimulatorState.DEINITIALIZED);

		INSTANCE = new Simulator();

		INSTANCE.executionStateNotifier = new Notifier<ExecutionState>(
				ExecutionState.class);
		INSTANCE.dptManager = new DPTManager();
		INSTANCE.database = new Database(modelStructure);
		INSTANCE.modelStructureCache = new ModelStructureCache();

		setSimulationState(SimulatorState.INITIALIZED);
	}

	private static SimulatorState simulatorState = SimulatorState.DEINITIALIZED;

	public enum SimulatorState {
		INITIALIZED, DEINITIALIZED
	};

	private static final void setSimulationState(SimulatorState simulatorState) {
		if (simulatorState == Simulator.simulatorState)
			return;

		Simulator.simulatorState = simulatorState;
		simulatorStateNotifier.notifySubscribers(simulatorState);
	}

	public static final void notifyError() {
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

	private LinkedList<TerminateCondition> terminateList = new LinkedList<TerminateCondition>();

	public static void addTerminateCondition(TerminateCondition c) {
		INSTANCE.terminateList.add(c);
	}

	private DPTManager dptManager;

	public static void addDecisionPoint(DecisionPoint dpt) {
		INSTANCE.dptManager.addDecisionPoint(dpt);
	}

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
		for (TerminateCondition c : terminateList)
			if (c.check())
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

	private int checkDPT() {
		while (dptManager.checkDPT() && !executionAborted) {
			notifyChange(ExecutionState.STATE_CHANGED);

			if (checkTerminate())
				return 1;
		}

		if (executionAborted)
			return -1;
		return 0;
	}

	public static int run() {
		isRunning = true;

		INSTANCE.database.addSystemEntry(Database.SystemEntryType.SIM_START);

		notifyChange(ExecutionState.EXECUTION_STARTED);
		notifyChange(ExecutionState.TIME_CHANGED);
		notifyChange(ExecutionState.STATE_CHANGED);

		int dptCheck = INSTANCE.checkDPT();
		if (dptCheck != 0)
			return stop(dptCheck);

		while (INSTANCE.eventScheduler.haveEvents()) {
			Event current = INSTANCE.eventScheduler.popEvent();

			INSTANCE.time = current.getTime();
			current.run();

			notifyChange(ExecutionState.TIME_CHANGED);
			notifyChange(ExecutionState.STATE_CHANGED);

			if (INSTANCE.checkTerminate())
				return stop(1);

			dptCheck = INSTANCE.checkDPT();
			if (dptCheck != 0)
				return stop(dptCheck);
		}
		return stop(0);
	}

	private static int stop(int code) {
		Database.SystemEntryType simFinishType;
		switch (code) {
		case -1:
			simFinishType = SystemEntryType.ABORT;
			break;
		case 0:
			simFinishType = SystemEntryType.NO_MORE_EVENTS;
			break;
		case 1:
			simFinishType = SystemEntryType.NORMAL_TERMINATION;
			break;
		default:
			simFinishType = SystemEntryType.RUN_TIME_ERROR;
			break;
		}
		INSTANCE.database.addSystemEntry(simFinishType);
		notifyChange(ExecutionState.EXECUTION_COMPLETED);
		isRunning = false;
		return code;
	}
}
