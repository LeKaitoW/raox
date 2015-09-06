package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulatorState;

public abstract class DefferedSubscriberManager<T> {
	public final void initialize(List<T> subscribersInfo) {
		initialize(subscribersInfo, EnumSet.noneOf(SubscriptionType.class));
	}

	public final void initialize(List<T> subscribersInfo,
			EnumSet<SubscriptionType> flags) {
		if (isInitialized)
			throw new NotifierException(
					"Attempting to initialize already initialized DefferedSubscriberManager");
		isInitialized = true;

		this.subscribersInfo.addAll(subscribersInfo);
		this.subscriptionFlags.addAll(flags);

		Simulator.getSimulatorStateNotifier().addSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED,
				EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED));
		Simulator.getSimulatorStateNotifier().addSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED,
				EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED));

		if (!subscriptionFlags.contains(SubscriptionType.IGNORE_ACCUMULATED)
				&& Simulator.isInitialized())
			initializationSubscriber.fireChange();
	}

	public final void deinitialize() {
		if (!isInitialized)
			throw new NotifierException(
					"Attempting to deinitialize not initialized DefferedSubscriberManager");
		isInitialized = false;

		if (Simulator.isInitialized() && needFire)
			deinitializationSubscriber.fireChange();

		Simulator.getSimulatorStateNotifier().removeSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED);
		Simulator.getSimulatorStateNotifier().removeSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED);

		subscribersInfo.clear();
	}

	private final Subscriber initializationSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			if (!needFire)
				return;

			initializationFired = true;

			registerExecutionSubscribers();
		}
	};

	protected abstract void registerExecutionSubscribers();

	private final Subscriber deinitializationSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			if (!needFire)
				return;

			if (subscriptionFlags.contains(SubscriptionType.IGNORE_ACCUMULATED)
					&& !initializationFired)
				return;

			unregisterExecutionSubscribers();
			if (subscriptionFlags.contains(SubscriptionType.ONE_SHOT)) {
				needFire = false;
				deinitialize();
			}
		}
	};

	protected abstract void unregisterExecutionSubscribers();

	protected Set<SubscriptionType> subscriptionFlags = EnumSet
			.noneOf(SubscriptionType.class);
	protected final Set<T> subscribersInfo = new HashSet<T>();
	private boolean initializationFired = false;
	private boolean needFire = true;
	private boolean isInitialized = false;
}
