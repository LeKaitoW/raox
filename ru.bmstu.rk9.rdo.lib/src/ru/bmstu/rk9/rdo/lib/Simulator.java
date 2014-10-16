package ru.bmstu.rk9.rdo.lib;

import java.util.LinkedList;

import ru.bmstu.rk9.rdo.lib.json.JSONObject;

public class Simulator
{
	private static Simulator INSTANCE = null;

	public static synchronized void initSimulation(JSONObject modelStructure)
	{
		if (isRunning)
			return;

		INSTANCE = new Simulator();

		INSTANCE.database = new Database(modelStructure);

		INSTANCE.notificationManager =
			new NotificationManager
			(
				new String[]
				{
					"StateChange",
					"TimeChange"
				}
			);

		DecisionPointSearch.allowSearch = true;
	}

	private Database database;

	public static Database getDatabase()
	{
		return INSTANCE.database;
	}

	private double time = 0;

	public static double getTime()
	{
		return INSTANCE.time;
	}

	private EventScheduler eventScheduler = new EventScheduler();

	public static void pushEvent(Event event)
	{
		INSTANCE.eventScheduler.pushEvent(event);
	}

	private LinkedList<TerminateCondition> terminateList = new LinkedList<TerminateCondition>();

	public static void addTerminateCondition(TerminateCondition c)
	{
		INSTANCE.terminateList.add(c);
	}

	private DPTManager dptManager = new DPTManager();

	public static void addDecisionPoint(DecisionPoint dpt)
	{
		INSTANCE.dptManager.addDecisionPoint(dpt);
	}

	private ResultManager resultManager = new ResultManager();

	public static void addResult(Result result)
	{
		INSTANCE.resultManager.addResult(result);
	}

	public static LinkedList<Result> getResults()
	{
		return INSTANCE.resultManager.getResults();
	}

	private NotificationManager notificationManager;

	public static Notifier getNotifier()
	{
		return INSTANCE.notificationManager;
	}

	private static void notifyChange(String category)
	{
		INSTANCE.notificationManager.notifySubscribers(category);
	}

	private boolean checkTerminate()
	{
		for (TerminateCondition c : terminateList)
			if (c.check())
				return true;
		return false;
	}

	private static volatile boolean isRunning = false;

	private volatile boolean executionAborted = false;

	public static synchronized void stopExecution()
	{
		if (INSTANCE == null)
			return;

		INSTANCE.executionAborted = true;
		INSTANCE.dptManager.dptAllowed = false;
		DecisionPointSearch.allowSearch = false;
	}

	private int checkDPT()
	{
		while (dptManager.checkDPT() && !executionAborted)
		{
			notifyChange("StateChange");

			if (checkTerminate())
				return  1;
		}

		if (executionAborted)
			return -1;
		return 0;
	}

	public static int run()
	{
		isRunning = true;

		notifyChange("StateChange");

		int dptCheck = INSTANCE.checkDPT();
		if (dptCheck != 0)
			return stop(dptCheck);

		while (INSTANCE.eventScheduler.haveEvents())
		{
			Event current = INSTANCE.eventScheduler.popEvent();

			INSTANCE.time = current.getTime();

			notifyChange("TimeChange");

			current.run();

			notifyChange("StateChange");

			if (INSTANCE.checkTerminate())
				return stop(1);

			dptCheck = INSTANCE.checkDPT();
			if (dptCheck != 0)
				return stop(dptCheck);
		}
		return stop(0);
	}

	private static int stop(int code)
	{
		isRunning = false;
		return code;
	}
}
