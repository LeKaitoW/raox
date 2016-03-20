package ru.bmstu.rk9.rao.ui.player;

import ru.bmstu.rk9.rao.lib.json.JSONArray;

public class Player {

	private void Delay(int time) {
		try {
			Thread.sleep(time); // 1000 milliseconds is one second.
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	public enum PlayingDirection {
		TOWARD, BACK;
	};

	private int PlaingSelector(int currentEventNumber, PlayingDirection playingDirection) {

		switch (playingDirection) {
		case TOWARD:
			// System.out.println("Playing toward!");
			currentEventNumber++;
			break;
		case BACK:
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

	public int Run(int currentEventNumber, int time, PlayingDirection playingDirection) {

		jsonModelStateObject = getModelData();

		String state = new String();
		state = "Play";

		while (state != "Stop" && state != "Pause" && (currentEventNumber <= (jsonModelStateObject.length() - 1))
				&& currentEventNumber > 0) {
			Delay(time);
			System.out.println("\n" + "Event number " + currentEventNumber + " Current frame"
					+ jsonModelStateObject.getJSONObject(currentEventNumber));
			currentEventNumber = PlaingSelector(currentEventNumber, playingDirection);

		}

		System.out.println("Playing done");
		return currentEventNumber;

	}

	public int Run() {
		return Run(0, 1000, PlayingDirection.TOWARD);
	}

	private JSONArray jsonModelStateObject = new JSONArray();
	private final Reader reader = new Reader();

}
