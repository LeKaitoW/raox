package ru.bmstu.rk9.rao.ui.notification;

import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulatorState;

public abstract class DefferedSubscriberManager {
	protected void initializeInternals() {
		Simulator.getSimulatorStateNotifier().addSubscriber(
				initializationSubscriber, SimulatorState.INITIALIZED, false);
		Simulator.getSimulatorStateNotifier()
				.addSubscriber(deinitializationSubscriber,
						SimulatorState.DEINITIALIZED, false);

		if (Simulator.isInitialized())
			initializationSubscriber.fireChange();
	}

	protected void deinitializeInternals() {
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
}
