package ru.bmstu.rk9.rao.ui.player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.google.gson.JsonObject;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.event.Event;
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
import ru.bmstu.rk9.rao.ui.player.gui.PlayerDelaySelectionToolbar;
import ru.bmstu.rk9.rao.ui.player.gui.PlayerSpeedSelectionToolbar;
import ru.bmstu.rk9.rao.ui.serialization.SerializationConfigView;

import java.lang.Math;

public class Player implements Runnable, ISimulator {

	private static final int dimension = 1;

	private static void Delay() {
		double time = 0;
		time = simulationDelays.get(currentEventNumber);
		if (speed > 0) {
			if (time == 0) {
				time += delay;

			}
			while (time == 0) {
				EventSwitcher();
				time = simulationDelays.get(currentEventNumber);
			}
		}
		if (speed < 0) {
			time = simulationDelays.get(currentEventNumber);

			if (time == 0) {
				time += delay;
			}
			while (time == 0) {
				EventSwitcher();
				time = simulationDelays.get(currentEventNumber);
			}
		}
		EventSwitcher();
		try {
			Thread.sleep((long) time * 1000 * dimension / Math.abs(speed));

		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public enum PlayingDirection {
		FORWARD, BACKWARD;
	};

	private static void EventSwitcher() {

		if (speed > 0) {

			currentEventNumber++;
		}
		if (speed < 0) {
			currentEventNumber--;
		}

	}

	private enum PlayerState {
		STOP, PLAY, PAUSE, INITIALIZED, FINISHED
	}

	private static void Timer() {

		if (speed >= 0) {
			Double simulationDelayForward = simulationDelays.get(currentEventNumber);
			simulationTime += simulationDelayForward;
		}
		if (speed < 0) {
			Double simulationDelayBackward = simulationDelays.get(currentEventNumber - 1);
			simulationTime -= simulationDelayBackward;
		}

	}

	public static List<ModelState> getModelData() {

		return reader.retrieveStateStorage();

	}

	public static JsonObject getModelStructure() {

		return parser.getSimulatorPreinitializationInfo().modelStructure;

	}

	public static void stop() {
		setInitialized(false);
		Player.currentEventNumber = 0;
		Player.simulationTime = 0.0;
		ConsoleView.clearConsoleText();
		Player.state = PlayerState.STOP;
		ConsoleView.addLine("Player status " + state);

	}

	public static void pause() {
		Player.state = PlayerState.PAUSE;
		ConsoleView.addLine("Player status " + state);

	}

	public static void initialize() {
		modelStateStorage.clear();
		simulationDelays.clear();
		modelStateStorage = getModelData();
		simulationDelays = reader.getSimulationDelays();
		direction = PlayingDirection.FORWARD;
		Thread thread = new Thread(new Player());
		thread.start();
		Player.state = PlayerState.PLAY;
		setInitialized(true);
		ConsoleView.addLine("Player status " + state);

	}

	public static void play() {
		direction = PlayingDirection.FORWARD;
		ConsoleView.addLine("Player status " + state + " direction " + direction);
		if (state == PlayerState.FINISHED || state == PlayerState.STOP || state == PlayerState.PAUSE) {
			Player.state = PlayerState.PLAY;
			Thread thread = new Thread(new Player());
			thread.start();
		}
	}

	public static void playBackward() {
		direction = PlayingDirection.BACKWARD;
		ConsoleView.addLine("Player status " + state + " direction " + direction);
		if (state == PlayerState.FINISHED || state == PlayerState.STOP || state == PlayerState.PAUSE) {
			Player.state = PlayerState.PLAY;
			Thread thread = new Thread(new Player());
			thread.start();
		}
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

	@SuppressWarnings("unused")
	private static boolean haveNewData = false;

	private final static Subscriber databaseSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewData = true;
		}
	};

	private synchronized static void runPlayer() {
		init();
		display.syncExec(() -> AnimationView.initialize(parser.getAnimationFrames()));
		Player.simulationTime = Reader.retrieveTimeStorage().get(currentEventNumber);
		CurrentSimulator.getDatabase().getNotifier().addSubscriber(databaseSubscriber,
				Database.NotificationCategory.ENTRY_ADDED);

		Player.computerTimeStart = (double) System.currentTimeMillis();
		while (state != PlayerState.STOP && state != PlayerState.PAUSE && state != PlayerState.FINISHED) {
			// Update run time on computer clock
			computerTime = (double) System.currentTimeMillis() - computerTimeStart;
			// Read user defined speed value

			if (direction == PlayingDirection.FORWARD) {
				speed = PlayerSpeedSelectionToolbar.getSpeed();
			} else {
				speed = -PlayerSpeedSelectionToolbar.getSpeed();
			}
			// Read user defined delay
			delay = PlayerDelaySelectionToolbar.getSpeed();
			// Advance simulation time
			Timer();
			// Get next event number
			Delay();

			if (simulationTime >= (reader.getLastLastTimeStorageElement()
					- simulationDelays.get(simulationDelays.size() - 1)) || simulationTime <= 0) {
				state = PlayerState.FINISHED;
				ConsoleView.addLine("Player status " + state);
			}
		}
		if (state == PlayerState.STOP) {
			currentEventNumber = 0;
			simulationTime = 0.0;
		}

	}

	public void run() {
		runPlayer();
		return;
	}

	final static Display display = PlatformUI.getWorkbench().getDisplay();
	private static IProject[] projectsInWorkspace = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	private static volatile PlayerState state = PlayerState.INITIALIZED;
	final static ModelInternalsParser parser = parseModel(getCurrentProject());
	private volatile static Integer currentEventNumber = 0;
	private static Reader reader = new Reader();
	private static List<ModelState> modelStateStorage = new ArrayList<>();
	private static JsonObject modelStructure = getModelStructure();
	private static Database database = null;
	private static List<Double> simulationDelays = new ArrayList<>();
	private static Double simulationTime = 0.0;
	private static Double computerTime = 0.0;
	private static Double computerTimeStart = 0.0;
	static PlayingDirection direction;
	private static int speed = PlayerSpeedSelectionToolbar.getSpeed();
	private static double delay = PlayerDelaySelectionToolbar.getSpeed();
	private static boolean initialized = false;

	public static void init() {
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
		return modelStateStorage.get(currentEventNumber);
	}

	@Override
	public void setModelState(ModelState modelState) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getTime() {

		System.out.println("computerTime " + computerTime * 0.001 + " s");
		System.out.println("simulationTime " + simulationTime);

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

	public static boolean isInitialized() {
		return initialized;
	}

	public static void setInitialized(boolean initialized) {
		Player.initialized = initialized;
	}

}
