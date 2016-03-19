package ru.bmstu.rk9.rao.ui.player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Serializer {

	public JSONObject dumpResoursestoJSONobject() {

		ModelState modelState = Simulator.getModelState();
		Collection<ResourceManager<? extends Resource>> listModelState = modelState.getResourceManagers();
		JSONObject jsonCurrentModelState = new JSONObject();
		JSONArray jsonResourses = new JSONArray();

		for (ResourceManager<? extends Resource> resourceManager : listModelState) {
			for (Resource resource : resourceManager.getAll()) {
				JSONObject jsonResourse = new JSONObject();
				jsonResourse.put("Resourse", resource.getName()).put("time ", Simulator.getTime());
				jsonResourses.put(jsonResourse);
			}

		}
		jsonCurrentModelState.put("Current resourses", jsonResourses);
		return jsonCurrentModelState;
	}

	public void dumpResoursestoJSONfile(JSONObject jsonObject) {
		try (FileWriter file = new FileWriter("/home/timur/JSON/test.json")) {
			file.write(jsonObject.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + jsonObject);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

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
