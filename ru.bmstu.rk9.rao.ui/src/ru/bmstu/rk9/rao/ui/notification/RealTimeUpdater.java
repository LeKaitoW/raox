package ru.bmstu.rk9.rao.ui.notification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.ExecutionState;
import ru.bmstu.rk9.rao.ui.simulation.SimulationModeDispatcher;
import ru.bmstu.rk9.rao.ui.simulation.SimulationSynchronizer.ExecutionMode;
import ru.bmstu.rk9.rao.ui.notification.SimulatorSubscriberManager.SimulatorSubscriberInfo;

public class RealTimeUpdater {

	public RealTimeUpdater() {
		subscriberRegistrationManager
				.initialize(new HashSet<SimulatorSubscriberInfo>(Arrays.asList(
						new SimulatorSubscriberInfo(simulationStartSubscriber,
								ExecutionState.EXECUTION_STARTED),
						new SimulatorSubscriberInfo(simulationStopSubscriber,
								ExecutionState.EXECUTION_COMPLETED))));
	}

	public final void deinitialize() {
		subscriberRegistrationManager.deinitialize();
	}

	public final void addScheduledAction(Runnable runnable) {
		scheduledActions.add(runnable);
	}

	public final void removeScheduledAction(Runnable runnable) {
		scheduledActions.remove(runnable);
	}

	private final void start() {
		display = PlatformUI.getWorkbench().getDisplay();
		Simulator
				.getDatabase()
				.getNotifier()
				.addSubscriber(databaseSubscriber,
						Database.NotificationCategory.ENTRY_ADDED);

		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				if (!haveNewData || paused || display.isDisposed())
					return;

				for (Runnable action : scheduledActions)
					display.asyncExec(action);
			}
		};
		timer.scheduleAtFixedRate(timerTask, delayMsec, periodMsec);
		paused = (SimulationModeDispatcher.getMode() == ExecutionMode.PAUSE);
	}

	private final void stop() {
		if (timer != null)
			timer.cancel();
		timer = null;
		timerTask = null;
		scheduledActions.clear();
	}

	private boolean haveNewData = false;

	private final SimulatorSubscriberManager subscriberRegistrationManager = new SimulatorSubscriberManager();

	private final Subscriber databaseSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewData = true;
		}
	};

	private final Subscriber simulationStartSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			start();
		}
	};

	private final Subscriber simulationStopSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			stop();
		}
	};

	private final Set<Runnable> scheduledActions = new HashSet<Runnable>();
	private Timer timer = null;
	private TimerTask timerTask = null;

	private final long delayMsec = 0;
	private final long periodMsec = 1000 / 25;

	private boolean paused = true;

	public final synchronized void setPaused(boolean paused) {
		if (this.paused == paused)
			return;

		this.paused = paused;
		if (timerTask != null)
			timerTask.run();
	}

	private Display display;
}
