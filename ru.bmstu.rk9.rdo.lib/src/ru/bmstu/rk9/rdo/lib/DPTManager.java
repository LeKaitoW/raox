package ru.bmstu.rk9.rdo.lib;

import java.util.Iterator;
import java.util.LinkedList;

class DPTManager implements Subscriber
{
	public DPTManager()
	{
		Simulator
			.getNotifier()
			.getSubscription("ExecutionAborted")
			.addSubscriber(this);
	}

	private LinkedList<DecisionPoint> dptList = new LinkedList<DecisionPoint>();

	void addDecisionPoint(DecisionPoint dpt)
	{
		dptList.add(dpt);
	}

	volatile private boolean dptAllowed = true;

	@Override
	public void fireChange()
	{
		this.dptAllowed = false;
	}

	boolean checkDPT()
	{
		Iterator<DecisionPoint> dptIterator = dptList.iterator();

		while(dptIterator.hasNext() && dptAllowed)
			if(dptIterator.next().check())
				return true;

		return false;
	}
}
