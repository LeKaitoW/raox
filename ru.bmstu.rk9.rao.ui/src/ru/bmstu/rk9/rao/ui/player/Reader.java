package ru.bmstu.rk9.rao.ui.player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ru.bmstu.rk9.rao.lib.simulator.ModelState;

public class Reader {

	public static List<ModelState> retrieveStateStorageFromFile() {

		File myFile = new File("/home/timur/JSON/test.json");
		FileInputStream fIn;
		List<ModelState> modelStateStorage = null;

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
			Gson gson = new Gson();

			Type collectionType = new TypeToken<ArrayList<ModelState>>() {
			}.getType();


			modelStateStorage = gson.fromJson(json, collectionType);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return modelStateStorage;

	}

}
