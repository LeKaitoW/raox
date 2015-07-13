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
import ru.bmstu.rk9.rao.lib.notification.NotificationManager;
import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.result.ResultManager;
import ru.bmstu.rk9.rao.lib.tracer.Tracer;
import ru.bmstu.rk9.rao.lib.treeBuilder.TreeBuilder;

public class Simulator {
	private static Simulator INSTANCE = null;

	public static synchronized void initSimulation(JSONObject modelStructure) {
		if (isRunning)
			return;

		INSTANCE = new Simulator();

		INSTANCE.notificationManager = new NotificationManager<NotificationCategory>(
				NotificationCategory.class);
		INSTANCE.dptManager = new DPTManager();
		INSTANCE.database = new Database(modelStructure);
		INSTANCE.modelStructureCache = new ModelStructureCache();
		INSTANCE.tracer = new Tracer();
		INSTANCE.treeBuilder = new TreeBuilder();
	}

	public static boolean isInitialized() {
		return INSTANCE != null;
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private Database database;

	public static Database getDatabase() {
		return INSTANCE.database;
	}

	private Tracer tracer;

	public static Tracer getTracer() {
		return INSTANCE.tracer;
	}

	private TreeBuilder treeBuilder;

	public static TreeBuilder getTreeBuilder() {
		return INSTANCE.treeBuilder;
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

	public enum NotificationCategory {
		EXECUTION_STARTED, EXECUTION_COMPLETED, EXECUTION_ABORTED, STATE_CHANGED, TIME_CHANGED
	};

	private NotificationManager<NotificationCategory> notificationManager;

	public static NotificationManager<NotificationCategory> getNotificationManager() {
		return INSTANCE.notificationManager;
	}

	private static void notifyChange(NotificationCategory category) {
		INSTANCE.notificationManager.notifySubscribers(category);
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
		if (INSTANCE == null)
			return;

		INSTANCE.executionAborted = true;
		notifyChange(NotificationCategory.EXECUTION_ABORTED);
	}

	private int checkDPT() {
		while (dptManager.checkDPT() && !executionAborted) {
			notifyChange(NotificationCategory.STATE_CHANGED);

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

		notifyChange(NotificationCategory.EXECUTION_STARTED);
		notifyChange(NotificationCategory.TIME_CHANGED);
		notifyChange(NotificationCategory.STATE_CHANGED);

		int dptCheck = INSTANCE.checkDPT();
		if (dptCheck != 0)
			return stop(dptCheck);

		while (INSTANCE.eventScheduler.haveEvents()) {
			Event current = INSTANCE.eventScheduler.popEvent();

			INSTANCE.time = current.getTime();

			notifyChange(NotificationCategory.TIME_CHANGED);

			current.run();

			notifyChange(NotificationCategory.STATE_CHANGED);

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
		notifyChange(NotificationCategory.EXECUTION_COMPLETED);
		isRunning = false;
		return code;
	}
}
