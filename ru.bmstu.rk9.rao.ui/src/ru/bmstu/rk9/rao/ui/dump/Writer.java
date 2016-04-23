package ru.bmstu.rk9.rao.ui.dump;

import java.util.Arrays;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;

public class Writer {

	public Writer() {
		initializeSubscribers();
	}

	private final void initializeSubscribers() {
		simulationSubscriberManager.initialize(
				Arrays.asList(new SimulatorSubscriberInfo(stateStorageSubscriber, ExecutionState.STATE_CHANGED),
						new SimulatorSubscriberInfo(simulationEndSubscriber, ExecutionState.EXECUTION_COMPLETED)));

	}

	public class StateStorageSubscriber implements Subscriber {
		public void fireChange() {
			modelStateStorage.addModelState(CurrentSimulator.getModelState());
		}

	}

	public class SimulationEndSubscriber implements Subscriber {
		public void fireChange() {
			stateStorageToString = Serializer.stateStorageToString(modelStateStorage);
			Serializer.writeStringToJsonFile(stateStorageToString);
		}

	}

	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
	}

	private final ModelStateStorage modelStateStorage = new ModelStateStorage();
	private String stateStorageToString = new String();
	public final SimulationEndSubscriber simulationEndSubscriber = new SimulationEndSubscriber();
	private final StateStorageSubscriber stateStorageSubscriber = new StateStorageSubscriber();
	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
}
