package ru.bmstu.rk9.rao.ui.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.lib.database.Database;
import ru.bmstu.rk9.rao.lib.notification.Subscriber;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class RealTimeUpdater {
	public final static void start() {
		display = PlatformUI.getWorkbench().getDisplay();
		Simulator.getDatabase().getNotificationManager()
				.getSubscription(Database.NotificationCategory.ENTRY_ADDED)
				.addSubscriber(databaseSubscriber);

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
		timer.scheduleAtFixedRate(timerTask, delay, period);
	}

	public final static void cancel() {
		timer.cancel();
		timer = null;
		timerTask = null;
		scheduledActions.clear();
	}

	public final static void addScheduledAction(Runnable runnable) {
		scheduledActions.add(runnable);
	}

	private static boolean haveNewData = false;

	private static final Subscriber databaseSubscriber = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewData = true;
		}
	};

	private static final List<Runnable> scheduledActions = new ArrayList<Runnable>();
	private static Timer timer = null;
	private static TimerTask timerTask = null;

	private static final long delay = 0;
	private static final long period = 100;

	private static boolean paused = true;

	public final static synchronized void setPaused(boolean paused) {
		if (RealTimeUpdater.paused == paused)
			return;

		RealTimeUpdater.paused = paused;
		if (timerTask != null)
			timerTask.run();
	}

	private static Display display;
}
