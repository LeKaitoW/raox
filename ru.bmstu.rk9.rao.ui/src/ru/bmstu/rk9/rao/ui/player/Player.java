package ru.bmstu.rk9.rao.ui.player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.execution.ModelInternalsParser;

public class Player implements Runnable, ISimulator {

	private void delay(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public enum PlayingDirection {
		FORWARD, BACKWARD;
	};

	private int PlaingSelector(int currentEventNumber, PlayingDirection playingDirection) {

		switch (playingDirection) {
		case FORWARD:
			currentEventNumber++;
			break;
		case BACKWARD:
			currentEventNumber--;
			break;

		default:
			System.out.println("Invalid grade");
		}

		return currentEventNumber;

	}

	public List<ModelState> getModelData() {

		return Reader.retrieveStateStorage();

	}

	public JSONObject getModelStructure() {

		return Reader.retrieveStructure();
	}

	public static void stop() {
		System.out.println("Stop");
		state = "Stop";
	}

	public static void pause() {
		System.out.println("Pause");
		state = "Pause";
	}

	public static void play() {
		Thread thread = new Thread(new Player());
		thread.start();
		System.out.println("Play");
		state = "Play";
	}

	private synchronized void runPlayer(int currentEventNumber, int time, PlayingDirection playingDirection) {

		modelStateStorage = getModelData();
		modelStructure = getModelStructure();

		CurrentSimulator.set(new Player());
		while (state != "Stop" && state != "Pause" && currentEventNumber < (modelStateStorage.size() - 1)
				&& currentEventNumber > 0) {
			delay(time);

			System.out.println("Event: " + modelStateStorage.get(currentEventNumber));
			currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);

			final Display display = PlatformUI.getWorkbench().getDisplay();
			IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			final ModelInternalsParser parser = new ModelInternalsParser(projectsInWorkspace[0]);
			try {
				parser.parse();
				parser.postprocess();

			} catch (IOException | NoSuchMethodException | SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassNotFoundException e) {
				e.printStackTrace();
			}

			AnimationView.setAnimationEnabled(true);
			display.syncExec(() -> AnimationView.initialize(parser.getAnimationFrames()));

		}
		System.out.println("Plaing done");
		currentEventNumber = 1;
		if (state == "Stop") {
			Player.currentEventNumber = 1;
		} else {
			Player.currentEventNumber = currentEventNumber;
		}

	}

	public void run() {

		runPlayer(currentEventNumber, 1000, PlayingDirection.FORWARD);

		return;
	}

	private static volatile String state = new String("");
	private volatile static Integer currentEventNumber = new Integer(1);
	private List<ModelState> modelStateStorage = new ArrayList<ModelState>();
	private JSONObject modelStructure = new JSONObject();

	@Override
	public void preinitilize(SimulatorPreinitializationInfo info) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(SimulatorInitializationInfo initializationInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public Database getDatabase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StaticModelData getStaticModelData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelState getModelState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setModelState(ModelState modelState) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void pushEvent(Event event) {
		// TODO Auto-generated method stub

	}

	private Notifier<ExecutionState> executionStateNotifier;

	@Override
	public Notifier<ExecutionState> getExecutionStateNotifier() {
		// TODO Auto-generated method stub
		return executionStateNotifier;
	}

	@Override
	public void notifyChange(ExecutionState category) {
		// TODO Auto-generated method stub
		executionStateNotifier.notifySubscribers(category);

	}

	@Override
	public void abortExecution() {
		// TODO Auto-generated method stub

	}

	@Override
	public SimulationStopCode runSimulator() {
		// TODO Auto-generated method stub
		return null;
	}

}
