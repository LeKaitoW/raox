package ru.bmstu.rk9.rao.lib.simulator;

import java.util.List;

import ru.bmstu.rk9.rao.lib.result.Result;
import ru.bmstu.rk9.rao.lib.simulator.Simulator.SimulationStopCode;

public abstract class EmbeddedSimulation {
	public static SimulationStopCode runSimulation(List<Result> results) {
		SimulationStopCode result = Simulator.run();

		results.addAll(Simulator.getResults());

		return result;
	}
}
