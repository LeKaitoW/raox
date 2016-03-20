package ru.bmstu.rk9.rao.ui.dump;

import java.util.Arrays;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorSubscriberManager.SimulatorSubscriberInfo;
import ru.bmstu.rk9.rao.ui.player.Player;
import ru.bmstu.rk9.rao.ui.player.Player.PlayingDirection;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
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
			modelStateStorage.addModelState(Simulator.getModelState());
			jsonModelStateArray.put(serializer.dumpResoursestoJSONobject());
		}

	}

	public class SimulationEndSubscriber implements Subscriber {
		public void fireChange() {
			JSONObject jsonModelStateObject = new JSONObject();
			jsonModelStateObject.put("Model state array", jsonModelStateArray);
			serializer.dumpResoursestoJSONfile(jsonModelStateObject);
			player.Run(5, 1000, PlayingDirection.BACK);
		}

	}

	public final void deinitializeSubscribers() {
		simulationSubscriberManager.deinitialize();
	}

	private final Player player = new Player();
	private final JSONArray jsonModelStateArray = new JSONArray();
	public final SimulationEndSubscriber simulationEndSubscriber = new SimulationEndSubscriber();
	private final Serializer serializer = new Serializer();
	private final ModelStateStorage modelStateStorage = new ModelStateStorage();
	private final StateStorageSubscriber stateStorageSubscriber = new StateStorageSubscriber();
	private final SimulatorSubscriberManager simulationSubscriberManager = new SimulatorSubscriberManager();
}
