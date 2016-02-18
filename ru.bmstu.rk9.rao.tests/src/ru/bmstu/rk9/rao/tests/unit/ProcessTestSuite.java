package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ru.bmstu.rk9.rao.lib.database.SerializationObjectsNames;
import ru.bmstu.rk9.rao.lib.json.JSONArray;
import ru.bmstu.rk9.rao.lib.json.JSONObject;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class ProcessTestSuite {

	public static void initEmptySimulation(){
		JSONObject modelStructure = new JSONObject().put("name", "")
				.put("resource_types", new JSONArray())
				.put("results", new JSONArray())
				.put("patterns", new JSONArray())
				.put("events", new JSONArray())
				.put("decision_points", new JSONArray());

		SerializationObjectsNames.set(new ArrayList<String>());
		Simulator.initSimulation(modelStructure);
	}
}
