package ru.bmstu.rk9.rdo.lib;

import java.util.LinkedList;

public abstract class Simulator
{
	private static double time = 0;

	public static double getTime()
	{
		return time;
	}

	private static EventScheduler eventScheduler = new EventScheduler();

	public static void pushEvent(Event event)
	{
		eventScheduler.pushEvent(event);
	}

	private static LinkedList<TerminateCondition> terminateList = new LinkedList<TerminateCondition>();

	public static void addTerminateCondition(TerminateCondition c)
	{
		terminateList.add(c);
	}

	private static DPTManager dptManager = new DPTManager();

	public static void addDecisionPoint(DecisionPoint dpt)
	{
		dptManager.addDecisionPoint(dpt);
	}

	private static ResultManager resultManager = new ResultManager();

	public static void addResult(Result result)
	{
		resultManager.addResult(result);
	}

	public static void getResults()
	{
		resultManager.getResults();
	}

	private static boolean checkTerminate()
	{
		for (TerminateCondition c : terminateList)
			if (c.check())
				return true;
		return false;
	}

	public static int run()
	{
		while (dptManager.checkDPT())
			if (checkTerminate())
				return 1;

		while (eventScheduler.haveEvents())
		{
			Event current = eventScheduler.popEvent();

			time = current.getTime();

			current.run();

			if (checkTerminate())
				return 1;

			while (dptManager.checkDPT())
				if (checkTerminate())
					return 1;
		}
		return 0;
	}
}
