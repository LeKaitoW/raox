package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.json.JSONObject;

public class SimulatorPreinitializationInfo {
	public final JSONObject modelStructure = new JSONObject();
	public final List<Class<?>> resourceClasses = new ArrayList<>();
}
