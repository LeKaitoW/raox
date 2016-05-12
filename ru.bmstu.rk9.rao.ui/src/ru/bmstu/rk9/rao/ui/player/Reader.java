package ru.bmstu.rk9.rao.ui.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.ui.dump.Serializer;

public class Reader {

	@SuppressWarnings("unchecked")

	public static List<ModelState> retrieveStateStorage() {
		File myFile = new File("/home/timur/JSON/test.json");
		FileInputStream fIn = null;
		List<ModelState> modelStateStorage = new ArrayList<ModelState>();
		ModelState modelState = new ModelState();
		try {
			fIn = new FileInputStream(myFile);
			InputStreamReader isr = new InputStreamReader(fIn);
			@SuppressWarnings("resource")
			BufferedReader bufferedReader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}

			String json = sb.toString();

			JsonParser parser = new JsonParser();
			JsonArray array = parser.parse(json).getAsJsonArray();
			for (int i = 0, size = array.size(); i < size; i++) {
				modelStateStorage.add(retrieveModelStateFromFile(fIn, myFile, (JsonObject) array.get(i)));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return modelStateStorage;
	}

	public static ModelState retrieveModelStateFromFile(FileInputStream fIn, File myFile, JsonObject data) {
		ModelState modelState = new ModelState();
		Gson gson = new Gson();
		URLClassLoader classLoader;
		
		try {
			URL modelURL = new URL("file:////home/timur/runtime-timur/resources/bin/");
			URL[] urls = new URL[] { modelURL };
			Collection<Class<? extends Resource>> listClass = new ArrayList<Class<? extends Resource>>();
			List<ComparableResource> listObject = new ArrayList<>();
			classLoader = new URLClassLoader(urls, CurrentSimulator.class.getClassLoader());

			Set<Map.Entry<String, JsonElement>> entries = data.get("resourceManagers").getAsJsonObject().entrySet();
			for (Map.Entry<String, JsonElement> entry : entries) {
				Set<Map.Entry<String, JsonElement>> resources = entry.getValue().getAsJsonObject().get("resources")
						.getAsJsonObject().entrySet();
				for (Map.Entry<String, JsonElement> resource : resources) {
					try {
						Class<?> forname = Class.forName(entry.getKey().substring(6), false, classLoader);
						ComparableResource<?> object = gson.fromJson(resource.getValue(), forname);
						listObject.add(object);
					} catch (JsonSyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					listClass.add(
							(Class<? extends Resource>) Class.forName(entry.getKey().substring(6), false, classLoader));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			modelState = new ModelState(listClass);
			for (ComparableResource object : listObject) {
				modelState.addResource(object);
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return modelState;

	}

}
