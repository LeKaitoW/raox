package ru.bmstu.rk9.rao.ui.player;

import java.util.Arrays;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;

public class ModelPlayer {

	public ModelPlayer() {
		initializeSubscribers();
	}

	private final void initializeSubscribers() {
		simulationSubscriberManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(stateStorageSubscriber, ExecutionState.STATE_CHANGED)));
	}

	public class StateStorageSubscriber implements Subscriber {
		public void fireChange() {
			modelStateStorage.addModelState(Simulator.getModelState());
			serializer.dumpResoursestoJSON();
		}

	}

	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
	}

	private final Serializer serializer = new Serializer();
	private final ModelStateStorage modelStateStorage = new ModelStateStorage();
	private final StateStorageSubscriber stateStorageSubscriber = new StateStorageSubscriber();
	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
}