package ru.bmstu.rk9.rao.lib.notification;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.notification.Subscription.SubscriptionType;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulatorState;

public abstract class DefferedSubscriberManager<T> {
	private enum InitializationState {
		INITIALIZED, NOT_INITIALIZED, UNDEFINED
	};

	private InitializationState initializationState = InitializationState.NOT_INITIALIZED;

	public final void initialize(List<T> subscribersInfo) {
		initialize(subscribersInfo, EnumSet.noneOf(SubscriptionType.class));
	}

	public final void initialize(List<T> subscribersInfo,
			EnumSet<SubscriptionType> flags) {
		if (initializationState != InitializationState.NOT_INITIALIZED)
			throw new NotifierException(
					"DefferedSubscriberManager should not be initialized, but "
							+ "it's state is " + initializationState);
		initializationState = InitializationState.UNDEFINED;

		this.subscribersInfo.addAll(subscribersInfo);
		this.subscriptionFlags.addAll(flags);

		Simulator.getSimulatorStateNotifier().addSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED,
				EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED));
		Simulator.getSimulatorStateNotifier().addSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED,
				EnumSet.of(SubscriptionType.IGNORE_ACCUMULATED));

		initializationState = InitializationState.INITIALIZED;

		if (!subscriptionFlags.contains(SubscriptionType.IGNORE_ACCUMULATED)
				&& Simulator.isInitialized())
			initializationSubscriber.fireChange();
	}

	public final void deinitialize() {
		if (initializationState != InitializationState.INITIALIZED)
			throw new NotifierException(
					"DefferedSubscriberManager should be initialized, but "
							+ "it's state is " + initializationState);
		initializationState = InitializationState.UNDEFINED;

		if (Simulator.isInitialized() && needFire)
			deinitializationSubscriber.fireChange();

		Simulator.getSimulatorStateNotifier().removeSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED);
		Simulator.getSimulatorStateNotifier().removeSubscriber(
				deinitializationSubscriber, SimulatorState.DEINITIALIZED);

		subscribersInfo.clear();
		initializationState = InitializationState.NOT_INITIALIZED;
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
}
