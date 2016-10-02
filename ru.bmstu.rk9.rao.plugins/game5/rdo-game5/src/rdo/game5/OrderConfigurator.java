package rdo.game5;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class OrderConfigurator {
	private static final int tilesCountX = 3;
	private static final int tilesCountY = 2;

	@SuppressWarnings("unchecked")
	public static void setInOrder(JSONObject object) throws IOException, ParseException {
		JSONParser parser = new JSONParser();
		InputStream inputStream = Game5View.class.getClassLoader()
				.getResourceAsStream(Game5ProjectConfigurator.configTemplatePath);
		InputStreamReader reader = new InputStreamReader(inputStream);
		JSONObject templateObject = (JSONObject) parser.parse(reader);
		JSONArray templateOrder = (JSONArray) templateObject.get("places");
		object.put("places", templateOrder);
	}

	public static JSONArray shuffle(JSONArray places, String solvable) {
		if (solvable.equals("all")) {
			Collections.shuffle(places);
			return places;
		}
		JSONArray order = inverseOrderPlaces(places);
		do {
			Collections.shuffle(order);
		} while (!solvable.equals(String.valueOf(isSolvable(tilesCountX, tilesCountY, order))));
		return inverseOrderPlaces(order);
	}

	private static boolean isSolvable(int tilesCountX, int tilesCountY, JSONArray order) {

		final int freePlaceRow = order.indexOf(String.valueOf(tilesCountX * tilesCountY)) / tilesCountX + 1;
		int sum = 0;
		for (int i = 0; i < tilesCountX * tilesCountY; i++) {
			int tileIndex = Integer.valueOf(order.get(i).toString());
			if (tileIndex == tilesCountX * tilesCountY)
				continue;
			for (int j = i + 1; j < tilesCountX * tilesCountY; j++) {
				if (tileIndex > Integer.valueOf(order.get(j).toString()))
					sum++;
			}
		}

		if (tilesCountX % 2 != 0) {
			return sum % 2 == 0;
		} else {
			return (tilesCountY % 2) == ((sum + freePlaceRow) % 2);
		}
		// См. Перельман. Живая математика.
	}

	public static String convertPlacesToString(JSONArray places) {
		String order = Integer.toString(places.indexOf(String.valueOf(1)) + 1);
		for (int i = 1; i < places.size(); i++) {
			order = order + " " + Integer.toString(places.indexOf(String.valueOf(i + 1)) + 1);
		}
		return order;
	}

	@SuppressWarnings("unchecked")
	public static JSONArray convertStringToPlaces(String order) {
		String[] orderList = order.split(" ");
		JSONArray orderArray = new JSONArray();
		if (!orderIsValid(orderList)) {
			return null;
		}
		for (int i = 0; i < orderList.length; i++) {
			orderArray.add(orderList[i]);
		}
		return inverseOrderPlaces(orderArray);
	}

	@SuppressWarnings("unchecked")
	public static JSONArray inverseOrderPlaces(JSONArray position) {
		JSONArray inversePosition = new JSONArray();
		for (int i = 1; i < position.size() + 1; i++)
			inversePosition.add(String.valueOf(position.indexOf(String.valueOf(i)) + 1));
		return inversePosition;
	}

	private static boolean orderIsValid(String[] orderList) {
		HashSet<Integer> standartOrder = new HashSet<>();

		for (int i = 0; i < tilesCountX * tilesCountY; i++) {
			standartOrder.add(i + 1);
		}
		for (int i = 0; i < orderList.length; i++) {
			try {
				if (!standartOrder.remove(Integer.valueOf(orderList[i]))) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return standartOrder.size() == 0;
	}
}
