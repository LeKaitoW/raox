package ru.bmstu.rk9.rao.ui.player;

import java.util.Arrays;

import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;


public class StateObserver {
			
	public StateObserver() {
	initializeSubscribers();
	}
	
	private final void initializeSubscribers() {
	simulationSubscriberManager.initialize(
			Arrays.asList(new SimulatorSubscriberInfo(stateStorageSubscriber, ExecutionState.STATE_CHANGED)));
	}
	
	public class StateStorageSubscriber implements Subscriber{
		 		public void fireChange() {
		 			modelStateStorage.addModelState(Simulator.getModelState());		 		}
		 		
	}
	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
	}
				
	ModelStateStorage modelStateStorage = new ModelStateStorage();
	public final StateStorageSubscriber stateStorageSubscriber = new StateStorageSubscriber();
	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
}