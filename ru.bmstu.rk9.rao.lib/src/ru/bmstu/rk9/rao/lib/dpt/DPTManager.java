package ru.bmstu.rk9.rao.lib.dpt;

import java.util.Iterator;
import java.util.LinkedList;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class DPTManager implements Subscriber {
	public DPTManager() {
		Simulator.getNotificationManager().getSubscription("ExecutionAborted")
				.addSubscriber(this);
	}

	private LinkedList<DecisionPoint> dptList = new LinkedList<DecisionPoint>();

	public void addDecisionPoint(DecisionPoint dpt) {
		dptList.add(dpt);
	}

	volatile private boolean dptAllowed = true;

	@Override
	public void fireChange() {
		this.dptAllowed = false;
	}

	public boolean checkDPT() {
		Iterator<DecisionPoint> dptIterator = dptList.iterator();

		while (dptIterator.hasNext() && dptAllowed)
			if (dptIterator.next().check())
				return true;

		return false;
	}
}
