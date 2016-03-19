package ru.bmstu.rk9.rao.ui.player;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class Player {

	public JSONArray getModelData() {

		return serializer.retrieveJSONobjectfromJSONfile();

	}

	public void PlayerRun() {

		jsonModelStateObject = getModelData();

		if (jsonModelStateObject != null && jsonModelStateObject.length() > 0) {
			for (int i = 0; i < jsonModelStateObject.length(); i++) {
				JSONObject jsonResourses = jsonModelStateObject.getJSONObject(i);
				System.out.println("\n" + "jsonResourses" + jsonResourses);

			}
		}

	}

	private JSONArray jsonModelStateObject = new JSONArray();
	private final Serializer serializer = new Serializer();

}
