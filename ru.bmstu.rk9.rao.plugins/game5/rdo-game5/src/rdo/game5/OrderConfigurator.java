package rdo.game5;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class OrderConfigurator {

	@SuppressWarnings("unchecked")
	public static void setInOrder(JSONObject object) {
		JSONParser parser = new JSONParser();
		final String configPath = "/model_template/config.json";
		try {
			InputStream inputStream = Game5View.class.getClassLoader()
					.getResourceAsStream(configPath);
			InputStreamReader reader = new InputStreamReader(inputStream);
			JSONObject templateObject = (JSONObject) parser.parse(reader);
			JSONArray templateOrder = (JSONArray) templateObject.get("places");
			object.put("places", templateOrder);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public static JSONArray shuffleSolvable(JSONArray places) {
		Collections.shuffle(places);
		final int tilesCountX = 3;
		final int tilesCountY = 2;
		final int freePlaceRow = places.indexOf(String.valueOf(6))
				/ tilesCountX + 1;
		int sum = 0;
		for (int i = 1; i < tilesCountX * tilesCountY; i++) {
			for (int j = i + 1; j < tilesCountX * tilesCountY; j++) {
				if (Integer.valueOf(places.get(i).toString()) > Integer
						.valueOf(places.get(j).toString()))
					sum++;
			}
		}
		while (!isSolvable(tilesCountX, tilesCountY, sum, freePlaceRow)) {
			Collections.shuffle(places);
		}
		return places;
	}

	public static JSONArray shuffle(JSONArray places) {
		Collections.shuffle(places);
		return places;
	}

	private static boolean isSolvable(int tilesCountX, int tilesCountY,
			int sum, int freePlaceRow) {
		boolean solvable = false;
		if (tilesCountX % 2 != 0) {
			if (sum % 2 == 0)
				solvable = true;
			else
				solvable = false;
		} else {
			if (tilesCountY % 2 != 0) {
				if ((sum + freePlaceRow) % 2 != 0)
					solvable = true;
				else
					solvable = false;
			} else {
				if ((sum + freePlaceRow) % 2 == 0)
					solvable = true;
				else
					solvable = false;
			}
		}
		return solvable;
	}
}
