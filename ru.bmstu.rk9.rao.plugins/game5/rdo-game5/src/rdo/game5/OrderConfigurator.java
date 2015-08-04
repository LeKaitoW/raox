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
	private static final int tilesCountX = 3;
	private static final int tilesCountY = 2;

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

	public static JSONArray shuffle(JSONArray places, boolean solvableOnly) {
		if (solvableOnly) {
			do {
				Collections.shuffle(places);
			} while (!isSolvable(tilesCountX, tilesCountY, places));
			return places;
		} else {
			Collections.shuffle(places);
			return places;
		}
	}

	private static boolean isSolvable(int tilesCountX, int tilesCountY,
			JSONArray places) {

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

		if (tilesCountX % 2 != 0) {
			return sum % 2 == 0;
		} else {
			return (tilesCountY % 2) == ((sum + freePlaceRow) % 2);
		}
	}

	public static String convertOrderToString(JSONArray places) {
		String order = Integer.toString(places.indexOf(String.valueOf(1)) + 1);
		for (int i = 1; i < places.size(); i++) {
			order = order
					+ " "
					+ Integer
							.toString(places.indexOf(String.valueOf(i + 1)) + 1);
		}
		return order;
	}

	@SuppressWarnings("unchecked")
	public static JSONArray convertOrderToJSONArray(String order) {
		String[] orderList = order.split(" ");
		JSONArray orderArray = new JSONArray();
		if (orderList.length != tilesCountX * tilesCountY) {
			return null;
		} else {
			for (int i = 0; i < orderList.length; i++) {
				orderArray.add(orderList[i]);
			}
			return orderArray;
		}
	}
}
