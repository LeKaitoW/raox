package ru.bmstu.rk9.rao.ui.player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class Reader {

	public JSONArray retrieveJSONobjectfromJSONfile() {
		String jsonData = "";
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader("/home/timur/JSON/test.json"));
			while ((line = br.readLine()) != null) {
				jsonData += line + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}
		JSONObject obj = new JSONObject(jsonData);
		JSONArray jsonModelStateObject = new JSONArray();
		jsonModelStateObject = obj.getJSONArray("Model state array");
		return jsonModelStateObject;
		// System.out.println("\n" + jsonModelStateObject);

	}
}