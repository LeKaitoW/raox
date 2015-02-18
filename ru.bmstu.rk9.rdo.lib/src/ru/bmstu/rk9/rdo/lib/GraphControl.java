package ru.bmstu.rk9.rdo.lib;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.bmstu.rk9.rdo.lib.Subscriber;

public class GraphControl {

	private volatile static boolean haveNewTimerTask = false;

	public static final Subscriber timerTaskUpdater = new Subscriber() {
		@Override
		public void fireChange() {
			haveNewTimerTask = true;
		}
	};

	public static ArrayList<Timer> timerList = new ArrayList<Timer>();

	public static TimerTask newTimerTask;
	
	private static int timerNum = 0;
	
	public static int getTimerNum() {
		return timerNum;
	}

	public static TimerTask getGraphRealTimeUpdaterTask() {

		return new TimerTask() {

			@Override
			public void run() {
				if (haveNewTimerTask) {
					haveNewTimerTask = false;
					timerList.add(new Timer());
					timerList.get(timerNum++).scheduleAtFixedRate(newTimerTask, 0, 10);
					System.out.println("GraphControl. Timer " + (timerNum - 1) + "started");
				}
			}
		};
	}
}
