package ru.bmstu.rk9.rao.tests.unit;

import java.util.ArrayList;

import ru.bmstu.rk9.rao.lib.database.SerializationObjectsNames;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class ProcessTestSuite {
	public static void initEmptySimulation() {
		SerializationObjectsNames.set(new ArrayList<String>());
		CurrentSimulator.set(new Simulator());
		CurrentSimulator.preinitialize();
	}
}
