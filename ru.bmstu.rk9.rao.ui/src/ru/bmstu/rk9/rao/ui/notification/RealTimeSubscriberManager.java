package ru.bmstu.rk9.rao.ui.notification;

import java.util.HashSet;
import java.util.Set;

import ru.bmstu.rk9.rao.ui.run.RuntimeComponents;

public class RealTimeSubscriberManager extends DefferedSubscriberManager {
	public final void initialize(Set<Runnable> subscribersInfo) {
		this.subscribersInfo.addAll(subscribersInfo);
		super.initializeInternals();
	}

	public final void deinitialize() {
		subscribersInfo.clear();
		super.deinitializeInternals();
	}

	private final Set<Runnable> subscribersInfo = new HashSet<Runnable>();

	@Override
	protected void registerExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.addScheduledAction(runnable);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (Runnable runnable : subscribersInfo)
			RuntimeComponents.realTimeUpdater.removeScheduledAction(runnable);
	}

}
