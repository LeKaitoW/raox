package ru.bmstu.rk9.rao.ui.player;

import ru.bmstu.rk9.rao.lib.json.JSONArray;

public class Player implements Runnable {

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
			// System.out.println("Playing toward!");
			currentEventNumber++;
			break;
		case BACKWARD:
			// System.out.println("Playing back");
			currentEventNumber--;
			break;

		default:
			System.out.println("Invalid grade");
		}

		return currentEventNumber;

	}

	public JSONArray getModelData() {

		return reader.retrieveJSONobjectfromJSONfile();

	}

	public static void stop() {
		System.out.println("Stop");
		state = "Stop";
	}

	public static void play() {
		Thread thread = new Thread(new Player());
		thread.start();
		System.out.println("Play");
		state = "Play";
	}

	private synchronized int runPlayer(int currentEventNumber, int time, PlayingDirection playingDirection) {

		jsonModelStateObject = getModelData();

		while (state != "Stop" && state != "Pause" && (currentEventNumber <= (jsonModelStateObject.length() - 1))
				&& currentEventNumber > 0) {
			Double currentFrameTime = jsonModelStateObject.getJSONObject(currentEventNumber)
					.getJSONArray("Current resourses").getJSONObject(1).getDouble("time ");
			delay(time);
			System.out
					.println("\n" + "Time " + currentFrameTime + "Event number " + currentEventNumber + " Current frame"
							+ jsonModelStateObject.getJSONObject(currentEventNumber).getJSONArray("Current resourses"));
			currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);

		}

		System.out.println("Playing done");
		return currentEventNumber;

	}

	public void run() {
		System.out.println("MyRunnable running from Player");
		runPlayer(1, 1000, PlayingDirection.FORWARD);

		return;
	}

	private static volatile String state = new String("");
	private JSONArray jsonModelStateObject = new JSONArray();
	private final Reader reader = new Reader();

}
