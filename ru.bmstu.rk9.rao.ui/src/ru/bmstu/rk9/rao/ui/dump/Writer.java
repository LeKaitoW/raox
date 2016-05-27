package ru.bmstu.rk9.rao.ui.dump;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
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
			modelStateStorage.add(CurrentSimulator.getModelState());
			timeStorage.add(CurrentSimulator.getTime());

		}

	}

	public class SimulationEndSubscriber implements Subscriber {
		public void fireChange() {

			String stateStorageToString = "";
			stateStorageToString = Serializer.stateStorageToString(modelStateStorage);
			Serializer.writeStringToJsonStateFile(stateStorageToString);
			String timeStorageToString = "";
			timeStorageToString = Serializer.timeStorageToString(timeStorage);
			Serializer.writeStringToJsonTimeFile(timeStorageToString);
			timeStorage.clear();
			modelStateStorage.clear();

		}

	}

	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
	}

	private Collection<Double> timeStorage = new ArrayList<>();
	private Collection<ModelState> modelStateStorage = new ArrayList<ModelState>();
	public final SimulationEndSubscriber simulationEndSubscriber = new SimulationEndSubscriber();
	private final StateStorageSubscriber stateStorageSubscriber = new StateStorageSubscriber();
	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
}