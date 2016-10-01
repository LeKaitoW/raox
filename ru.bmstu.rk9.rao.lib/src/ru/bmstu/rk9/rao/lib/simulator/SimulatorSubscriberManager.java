package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.notification.DefferedSubscriberManager;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;

public class SimulatorSubscriberManager extends DefferedSubscriberManager<SimulatorSubscriberInfo> {
	public static class SimulatorSubscriberInfo {
		public SimulatorSubscriberInfo(Subscriber subscriber, CurrentSimulator.ExecutionState notificationCategory) {
			this.subscriber = subscriber;
			this.notificationCategory = notificationCategory;
		}

		final Subscriber subscriber;
		final CurrentSimulator.ExecutionState notificationCategory;
	}

	@Override
	protected void registerExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : subscribersInfo)
			CurrentSimulator.getExecutionStateNotifier().addSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : subscribersInfo)
			CurrentSimulator.getExecutionStateNotifier().removeSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}
}
