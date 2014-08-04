package ru.bmstu.rk9.rdo.lib;

import java.util.LinkedList;

public class Simulator
{
	private static Simulator INSTANCE = null;

	public static void initSimulation()
	{
		if (isRunning)
			return;

		INSTANCE = new Simulator();
		DecisionPointSearch.allowSearch = true;
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

	public static void printResults()
	{
		INSTANCE.resultManager.printResults();
	}

	public static LinkedList<Result> getResults()
	{
		return INSTANCE.resultManager.getResults();
	}

	private boolean checkTerminate()
	{
		for (TerminateCondition c : terminateList)
			if (c.check())
				return true;
		return false;
	}

	private static boolean isRunning = false;

	private boolean executionAborted = false;

	public static void stopExecution()
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
			if (checkTerminate())
				return  1;
		if (executionAborted)
			return -1;
		return 0;
	}

	public static int run()
	{
		isRunning = true;

		int dptCheck = INSTANCE.checkDPT();
		if (dptCheck != 0)
			return stop(dptCheck);

		while (INSTANCE.eventScheduler.haveEvents())
		{
			Event current = INSTANCE.eventScheduler.popEvent();

			INSTANCE.time = current.getTime();

			current.run();

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
