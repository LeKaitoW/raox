package ru.bmstu.rk9.rao.lib.simulator;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.dpt.DPTManager;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.event.EventScheduler;
import ru.bmstu.rk9.rao.lib.logger.Logger;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.process.Process;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;
import ru.bmstu.rk9.rao.lib.result.ResultManager;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;

public class Simulator implements ISimulator {
	@Override
	public void preinitilize(SimulatorPreinitializationInfo preinitializationInfo) {
		modelState = new ModelState(preinitializationInfo.resourceClasses);
		database = new Database(preinitializationInfo.modelStructure);
		staticModelData = new StaticModelData(preinitializationInfo.modelStructure);
		logger = new Logger();

		for (Runnable resourcePreinitializer : preinitializationInfo.resourcePreinitializers)
			resourcePreinitializer.run();
	}

	@Override
	public void initialize(SimulatorInitializationInfo initializationInfo) {
		executionStateNotifier = new Notifier<ExecutionState>(ExecutionState.class);
		dptManager = new DPTManager(initializationInfo.decisionPoints);
		processManager = new Process(initializationInfo.processBlocks);
		resultManager = new ResultManager(initializationInfo.results);
		time = initializationInfo.getTimeStart();
		timeFormatter = initializationInfo.getTimeFormatter();
		database.setTimeFormatter(timeFormatter);

		for (Supplier<Boolean> terminateCondition : initializationInfo.terminateConditions)
			terminateList.add(terminateCondition);

		for (Runnable init : initializationInfo.initList)
			init.run();

		database.addMemorizedResourceEntries(null, null, null);
	}

	private Database database;

	@Override
	public Database getDatabase() {
		return database;
	}

	private StaticModelData staticModelData;

	@Override
	public StaticModelData getStaticModelData() {
		return staticModelData;
	}

	private ModelState modelState;

	@Override
	public ModelState getModelState() {
		return modelState;
	}

	@Override
	public void setModelState(ModelState modelState) {
		this.modelState = modelState;
	}

	private volatile double time = 0;

	@Override
	public double getTime() {
		return time;
	}

	private Function<Double, String> timeFormatter;

	@Override
	public String formatTime(double time, Function<Double, String> defaultFormatter) {
		if (SimulatorInitializationInfo.DEFAULT_TIME_FORMATTER == timeFormatter)
			return defaultFormatter.apply(time);
		else
			return timeFormatter.apply(time);
	}

	private EventScheduler eventScheduler = new EventScheduler();

	@Override
	public void pushEvent(Event event) {
		eventScheduler.pushEvent(event);
	}

	private List<Supplier<Boolean>> terminateList = new LinkedList<>();

	private DPTManager dptManager;

	private Process processManager;

	private ResultManager resultManager;

	@Override
	public List<AbstractResult<?>> getResults() {
		return resultManager.getResults();
	}

	private Logger logger;

	@Override
	public Logger getLogger() {
		return logger;
	}

	private Notifier<ExecutionState> executionStateNotifier;

	@Override
	public Notifier<ExecutionState> getExecutionStateNotifier() {
		return executionStateNotifier;
	}

	@Override
	public void notifyChange(ExecutionState category) {
		executionStateNotifier.notifySubscribers(category);
	}

	private volatile boolean executionAborted = false;

	@Override
	public void abortExecution() {
		executionAborted = true;
	}

	@Override
	public SimulationStopCode run() {
		database.addSystemEntry(Database.SystemEntryType.SIM_START);

		notifyChange(ExecutionState.EXECUTION_STARTED);
		notifyChange(ExecutionState.TIME_CHANGED);
		notifyChange(ExecutionState.STATE_CHANGED);

		while (!executionAborted) {
			if (checkTerminate())
				return SimulationStopCode.TERMINATE_CONDITION;

			if (dptManager.checkDPT()) {
				notifyChange(ExecutionState.STATE_CHANGED);
				continue;
			}

			ProcessStatus processStatus = processManager.scan();
			if (processStatus == ProcessStatus.SUCCESS) {
				notifyChange(ExecutionState.STATE_CHANGED);
				continue;
			} else if (processStatus == ProcessStatus.FAILURE) {
				return SimulationStopCode.RUNTIME_ERROR;
			}

			if (!eventScheduler.haveEvents())
				return SimulationStopCode.NO_MORE_EVENTS;

			Event event = eventScheduler.popEvent();
			time = event.getTime();
			event.run();

			notifyChange(ExecutionState.TIME_CHANGED);
			notifyChange(ExecutionState.STATE_CHANGED);
		}

		return SimulationStopCode.USER_INTERRUPT;
	}

	private boolean checkTerminate() {
		for (Supplier<Boolean> c : terminateList)
			if (c.get())
				return true;
		return false;
	}
}
