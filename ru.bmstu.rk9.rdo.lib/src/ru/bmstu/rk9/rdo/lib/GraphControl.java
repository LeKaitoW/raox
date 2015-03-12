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
					timerList.get(timerNum++).scheduleAtFixedRate(newTimerTask, 0, 1);
					System.out.println("GraphControl. Timer " + (timerNum - 1) + "started");
				}
			}
		};
	}
	private static Integer lastAddedVertexIndex;
	
	public static Integer getLastAddedVertexIndex() {
		return lastAddedVertexIndex;
	}

	public static void setLastAddedVertexIndex(Integer lastAddedVertex) {
		GraphControl.lastAddedVertexIndex = lastAddedVertex;
	}
	
	private static Integer dptNumOfLastAddedVertex;
	
	public static Integer getDptNumOfLastAddedVertex() {
		return dptNumOfLastAddedVertex;
	}

	public static void setDptNumOfLastAddedVertex(Integer dptNumOflastAddedVertex) {
		GraphControl.dptNumOfLastAddedVertex = dptNumOflastAddedVertex;
	}
}
