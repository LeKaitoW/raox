package ru.bmstu.rk9.rao.ui.notification;

import java.util.HashSet;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulatorState;
import ru.bmstu.rk9.rao.ui.run.RuntimeComponents;

public class SubscriberRegistrationManager {
	class CommonSubscriberInfo {
		CommonSubscriberInfo(Subscriber subscriber,
				Simulator.ExecutionState notificationCategory) {
			this.subscriber = subscriber;
			this.notificationCategory = notificationCategory;
		}

		final Subscriber subscriber;
		final Simulator.ExecutionState notificationCategory;
	}

	public final SubscriberRegistrationManager enlistCommonSubscriber(
			Subscriber subscriber, Simulator.ExecutionState notificationCategory) {
		subscribersInfo.add(new CommonSubscriberInfo(subscriber,
				notificationCategory));
		return this;
	}

	public final SubscriberRegistrationManager enlistRealTimeSubscriber(
			Runnable runnable) {
		realTimeSubscribersInfo.add(runnable);
		return this;
	}

	private final Set<CommonSubscriberInfo> subscribersInfo = new HashSet<CommonSubscriberInfo>();
	private final Set<Runnable> realTimeSubscribersInfo = new HashSet<Runnable>();

	public final void initialize() {
		Simulator.getSimulatorStateNotifier().addSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED);
		Simulator.getSimulatorStateNotifier().addSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED);
	}

	public final void deinitialize() {
		Simulator.getSimulatorStateNotifier().removeSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED);
		Simulator.getSimulatorStateNotifier().removeSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED);
	}

	private final Subscriber initializationSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			registerExecutionSubscribers();
		}
	};

	private void registerExecutionSubscribers() {
		for (CommonSubscriberInfo subscriberInfo : subscribersInfo)
			Simulator.getExecutionStateNotifier().addSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
		for (Runnable runnable : realTimeSubscribersInfo)
			RuntimeComponents.realTimeUpdater.addScheduledAction(
					runnable);
	}

	private final Subscriber deinitializationSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			unregisterExecutionSubscribers();
		}
	};

	private void unregisterExecutionSubscribers() {
		for (CommonSubscriberInfo subscriberInfo : subscribersInfo)
			Simulator.getExecutionStateNotifier().removeSubscriber(subscriberInfo.subscriber,
					subscriberInfo.notificationCategory);
		for (Runnable runnable : realTimeSubscribersInfo)
			RuntimeComponents.realTimeUpdater.removeScheduledAction(
					runnable);
	}
}
