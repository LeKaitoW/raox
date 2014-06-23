package ru.bmstu.rk9.rdo.lib;

import java.util.Iterator;
import java.util.LinkedList;

class DPTManager
{
	private LinkedList<DecisionPoint> dptList = new LinkedList<DecisionPoint>();

	void addDecisionPoint(DecisionPoint dpt)
	{
		dptList.add(dpt);
	}

	boolean checkDPT()
	{
		Iterator<DecisionPoint> dptIterator = dptList.iterator();

		while (dptIterator.hasNext() && !Simulator.isExecutionAborted())
			if (dptIterator.next().check())
				return true;

		return false;
	}
}
