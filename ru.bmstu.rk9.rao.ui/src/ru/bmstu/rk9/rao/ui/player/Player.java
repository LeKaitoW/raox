package ru.bmstu.rk9.rao.ui.player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.modeldata.StaticModelData;
import ru.bmstu.rk9.rao.lib.notification.Notifier;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.ExecutionState;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator.SimulationStopCode;
import ru.bmstu.rk9.rao.lib.simulator.ISimulator;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorInitializationInfo;
import ru.bmstu.rk9.rao.lib.simulator.SimulatorPreinitializationInfo;
import ru.bmstu.rk9.rao.ui.animation.AnimationView;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;
import ru.bmstu.rk9.rao.ui.execution.ModelInternalsParser;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;
import ru.bmstu.rk9.rao.ui.simulation.StatusView;

public class Player implements Runnable, ISimulator {

	private static final int scalingFactor = 50;

	private int delay(int currentEventNumber, PlayingDirection playingDirection, double simultaneousEventsDelay) {
		double time = 0;
		switch (playingDirection) {
		case FORWARD:
			time = simulationDelays.get(currentEventNumber);
			if (time == 0) {
				time += simultaneousEventsDelay;

			}
			while (time == 0) {
				currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);
				time = simulationDelays.get(currentEventNumber);
			}
			break;
		case BACKWARD:
			time = simulationDelays.get(currentEventNumber);
			if (time == 0) {
				time += simultaneousEventsDelay;
			}
			while (time == 0) {
				currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);
				time = simulationDelays.get(currentEventNumber);
			}
			break;

		default:
			System.out.println("Invalid grade");
		}

		try {
			Thread.sleep((long) time * scalingFactor);

		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		return currentEventNumber;
	}

	public enum PlayingDirection {
		FORWARD, BACKWARD;
	};

	private enum PlayerState {
		STOP, PLAY, PAUSE, INITIALIZED, FINISHED
	}

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

	private Double TimerSelector(Double timer, int currentEventNumber, PlayingDirection playingDirection) {

		switch (playingDirection) {
		case FORWARD:
			Double simulationDelayForward = simulationDelays.get(currentEventNumber);
			timer += simulationDelayForward;
			break;
		case BACKWARD:
			Double simulationDelayBackward = simulationDelays.get(currentEventNumber - 1);
			timer -= simulationDelayBackward;
			break;

		default:
			System.out.println("Invalid grade");
		}

		return timer;

	}

	public static List<ModelState> getModelData() {

		return reader.retrieveStateStorage();

	}

	public JsonObject getModelStructure() {

		return parser.getSimulatorPreinitializationInfo().modelStructure;

	}

	public static void stop() {
		Player.currentEventNumber = 0;
		Player.simulationTime = 0.0;
		ConsoleView.clearConsoleText();
		state = PlayerState.STOP;
		System.out.println("You are pushing stop");

	}

	public static void pause() {
		state = PlayerState.PAUSE;
		System.out.println("You are pushing pause");

	}

	public static void play() {
		System.out.println("You are pushing play");

		modelStateStorage.clear();
		simulationDelays.clear();
		modelStateStorage = getModelData();
		simulationDelays = reader.getSimulationDelays();
		direction = PlayingDirection.FORWARD;
		Thread thread = new Thread(new Player());
		thread.start();
		state = PlayerState.PLAY;

	}

	public static void playBackward() {
		System.out.println("You are pushing playBackward");

		modelStateStorage.clear();
		simulationDelays.clear();
		modelStateStorage = getModelData();
		simulationDelays = reader.getSimulationDelays();
		direction = PlayingDirection.BACKWARD;
		Thread thread = new Thread(new Player());
		thread.start();
		state = PlayerState.PLAY;

	}

	private static ModelInternalsParser parseModel(IProject projectsInWorkspace) {
		final ModelInternalsParser parser = new ModelInternalsParser(projectsInWorkspace);
		try {
			parser.parse();
			parser.postprocess();

		} catch (IOException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| ClassNotFoundException e) {
			e.printStackTrace();
		}

		AnimationView.setAnimationEnabled(true);

		return parser;
	}

	public static IProject getCurrentProject() {
		IProject currentProject = projectsInWorkspace[0];
		return currentProject;
	}

	public static String getCurrentProjectPath() {
		String path = getCurrentProject().getLocation().toString();
		return path;
	}

	private boolean haveNewData = false;

	private final Subscriber databaseSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewData = true;
		}
	};

	private synchronized void runPlayer(int currentEventNumber, PlayingDirection playingDirection,
			double simultaneousEventsDelay) {
		init();
		display.syncExec(() -> AnimationView.initialize(parser.getAnimationFrames()));
		Player.simulationTime = reader.retrieveTimeStorage().get(currentEventNumber);
		// System.out.println("Player.timer " + Player.simulationTime);

		CurrentSimulator.getDatabase().getNotifier().addSubscriber(databaseSubscriber,
				Database.NotificationCategory.ENTRY_ADDED);

		System.out.println("state " + state + " currentEventNumber " + currentEventNumber + " modelStateStorage.size() "
				+ modelStateStorage.size() + " ");
		ConsoleView.addLine("Player status " + state);
		Player.computerTimeStart = (double) System.currentTimeMillis();
		while (state != PlayerState.STOP && state != PlayerState.PAUSE && state != PlayerState.FINISHED
				&& currentEventNumber < (modelStateStorage.size() - 1) && currentEventNumber >= 0) {
			Player.computerTime = (double) System.currentTimeMillis() - computerTimeStart;
			currentEventNumber = delay(currentEventNumber, playingDirection, simultaneousEventsDelay);
			Player.simulationTime = TimerSelector(Player.simulationTime, currentEventNumber, playingDirection);
			currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);

			if (currentEventNumber == modelStateStorage.size() - 1) {
				currentEventNumber = 0;
				state = PlayerState.FINISHED;
			}
		}
		if (state == PlayerState.STOP) {
			Player.currentEventNumber = 0;
			Player.simulationTime = 0.0;
		} else {
			Player.currentEventNumber = currentEventNumber;
		}

	}

	public void run() {
		runPlayer(currentEventNumber, direction, 10);
		return;
	}

	final static Display display = PlatformUI.getWorkbench().getDisplay();
	private static IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	private static volatile PlayerState state = PlayerState.INITIALIZED;
	final static ModelInternalsParser parser = parseModel(getCurrentProject());
	private volatile static Integer currentEventNumber = new Integer(0);
	private static Reader reader = new Reader();
	private static List<ModelState> modelStateStorage = new ArrayList<>();
	private JsonObject modelStructure = getModelStructure();
	private static Database database = null;
	private static List<Double> simulationDelays = new ArrayList<>();
	private static Double simulationTime = 0.0;
	private static Double computerTime = 0.0;
	private static Double computerTimeStart = 0.0;
	static PlayingDirection direction;

	public void init() {
		SerializationConfigView.initNames();
		CurrentSimulator.set(new Player());
		database = new Database(modelStructure);
	}

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

		return database;
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

		// System.out.println("computerTime " + computerTime * 0.001 + " s");
		// System.out.println("simulationTime " + simulationTime);

		return simulationTime;
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
