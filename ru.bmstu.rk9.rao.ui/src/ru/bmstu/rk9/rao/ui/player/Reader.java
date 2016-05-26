package ru.bmstu.rk9.rao.ui.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.ui.console.ConsoleView;

public class Reader {

	@SuppressWarnings("unchecked")
	public List<ModelState> retrieveStateStorage() {
		File myFile = new File(Player.getCurrentProjectPath() + "/stateStorage.json");
		FileInputStream fileInputStream = null;
		List<ModelState> modelStateStorage = new ArrayList<ModelState>();
		ModelState modelState = new ModelState();
		try {
			fileInputStream = new FileInputStream(myFile);
			InputStreamReader isr = new InputStreamReader(fileInputStream);
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
			for (int i = 0; i < array.size(); i++) {
				modelStateStorage
						.add(retrieveModelStateFromFile(fileInputStream, myFile, array.get(i).getAsJsonObject()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			ConsoleView.addLine("Invalide JSON with model states");
		}

		return modelStateStorage;
	}

	public List<Double> retrieveTimeStorage() {
		File myFile = new File(Player.getCurrentProjectPath() + "/timeStorage.json");
		FileInputStream fileInputStream = null;
		List<Double> timeStorage = new ArrayList<>();
		try {
			fileInputStream = new FileInputStream(myFile);
			InputStreamReader isr = new InputStreamReader(fileInputStream);
			@SuppressWarnings("resource")
			BufferedReader bufferedReader = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}

			String json = sb.toString();
			Gson gson = new Gson();
			Type collectionType = new TypeToken<List<Double>>() {
			 }.getType();
			 timeStorage = gson.fromJson(json, collectionType);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeStorage;
	}

	public List<Double> getSimulationDelays() {

		List<Double> timeStorage = retrieveTimeStorage();
		List<Double> simulationDelays = new ArrayList<>();

		Iterator<Double> i = timeStorage.iterator();
		Double prev = i.next();
		while (i.hasNext()) {
			Double curr = i.next();
			Double delta = curr - prev;
			simulationDelays.add(delta);
			prev = curr;
		}
		return simulationDelays;

	}

	private static final int classNameOffset = "class ".length();

	private ModelState retrieveModelStateFromFile(FileInputStream fIn, File myFile, JsonObject data) {
		ModelState modelState = new ModelState();
		Gson gson = new Gson();
		URLClassLoader classLoader;

		try {
			String resourcesPath = Player.getCurrentProjectPath();
			URL modelURL = new URL("file:///" + resourcesPath + "/.." + "/resources/bin/");
			URL[] urls = new URL[] { modelURL };
			Collection<Class<? extends Resource>> listClass = new ArrayList<>();
			List<ComparableResource> listObject = new ArrayList<>();
			classLoader = new URLClassLoader(urls, CurrentSimulator.class.getClassLoader());

			Set<Map.Entry<String, JsonElement>> entries = data.get("resourceManagers").getAsJsonObject().entrySet();
			for (Map.Entry<String, JsonElement> entry : entries) {
				Set<Map.Entry<String, JsonElement>> resources = entry.getValue().getAsJsonObject().get("resources")
						.getAsJsonObject().entrySet();
				for (Map.Entry<String, JsonElement> resource : resources) {
					Class<?> resourceClass = Class.forName(entry.getKey().substring(classNameOffset), false,
							classLoader);
					ComparableResource<?> comparableResource = gson.fromJson(resource.getValue(), resourceClass);
					listObject.add(comparableResource);
				}

				listClass.add((Class<? extends Resource>) Class.forName(entry.getKey().substring(classNameOffset),
						false, classLoader));
			}
			modelState = new ModelState(listClass);
			for (ComparableResource object : listObject) {
				modelState.addResource(object);
			}

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return modelState;

	}

}
