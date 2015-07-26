package ru.bmstu.rk9.rao.ui.notification;

import java.util.HashSet;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class SimulatorSubscriberManager extends DefferedSubscriberManager {
	public static class SimulatorSubscriberInfo {
		public SimulatorSubscriberInfo(Subscriber subscriber,
				Simulator.ExecutionState notificationCategory) {
			this.subscriber = subscriber;
			this.notificationCategory = notificationCategory;
		}

		final Subscriber subscriber;
		final Simulator.ExecutionState notificationCategory;
	}

	public final void initialize(
			Set<SimulatorSubscriberInfo> simulatorSubscribersInfo) {
		this.simulatorSubscribersInfo.addAll(simulatorSubscribersInfo);
		super.initializeInternals();
	}

	public final void deinitialize() {
		simulatorSubscribersInfo.clear();
		super.deinitializeInternals();
	}

	private final Set<SimulatorSubscriberInfo> simulatorSubscribersInfo = new HashSet<SimulatorSubscriberInfo>();

	@Override
	protected void registerExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : simulatorSubscribersInfo)
			Simulator.getExecutionStateNotifier().addSubscriber(
					subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}

	@Override
	protected void unregisterExecutionSubscribers() {
		for (SimulatorSubscriberInfo subscriberInfo : simulatorSubscribersInfo)
			Simulator.getExecutionStateNotifier().removeSubscriber(
					subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
	}
}
