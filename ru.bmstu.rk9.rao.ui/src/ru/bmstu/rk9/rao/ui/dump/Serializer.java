package ru.bmstu.rk9.rao.ui.dump;

import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Serializer {

	public static String stateStorageToString(ModelStateStorage modelStateStorage) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(modelStateStorage);
		return json;
		
	}

	public static void writeStringToJsonFile(String string) {
		try (FileWriter file = new FileWriter("/home/timur/JSON/test.json")) {
			file.write(string);
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("\nJSON Object: " + string);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}

}
