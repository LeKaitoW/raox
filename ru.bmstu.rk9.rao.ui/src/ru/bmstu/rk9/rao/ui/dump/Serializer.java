package ru.bmstu.rk9.rao.ui.dump;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ru.bmstu.rk9.rao.lib.simulator.ModelState;
import ru.bmstu.rk9.rao.ui.player.Player;

public class Serializer {

	public static String stateStorageToString(Collection<ModelState> modelStateStorage) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(modelStateStorage);
		return json;

	}

	public static void writeStringToJsonStateFile(String string) {
		try (FileWriter file = new FileWriter(Player.getCurrentProjectPath() + "/stateStorage.json")) {
			file.write(string);
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + string);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

}
