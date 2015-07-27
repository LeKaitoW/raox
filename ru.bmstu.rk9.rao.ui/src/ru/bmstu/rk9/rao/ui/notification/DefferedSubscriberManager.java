package ru.bmstu.rk9.rao.ui.notification;

import java.util.HashSet;
import java.util.Set;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulatorState;

public abstract class DefferedSubscriberManager<T> {
	public final void initialize(Set<T> subscribersInfo) {
		this.subscribersInfo.addAll(subscribersInfo);

		Simulator.getSimulatorStateNotifier().addSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED, false);
		Simulator.getSimulatorStateNotifier()
				.addSubscriber(deinitializationSubscriber,
						SimulatorState.DEINITIALIZED, false);

		if (Simulator.isInitialized())
			initializationSubscriber.fireChange();
	}

	public final void deinitialize() {
		subscribersInfo.clear();

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

	protected abstract void registerExecutionSubscribers();

	private final Subscriber deinitializationSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			unregisterExecutionSubscribers();
		}
	};

	protected abstract void unregisterExecutionSubscribers();

	protected final Set<T> subscribersInfo = new HashSet<T>();
}
