package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class SimulatorInitializationInfo {
	private final List<Runnable> initList = new ArrayList<>();
	private final List<Supplier<Boolean>> terminateConditions = new ArrayList<>();
	private final List<AbstractDecisionPoint> decisionPoints = new ArrayList<>();
	private final List<Block> processBlocks = new ArrayList<>();
	private final List<AbstractResult<?>> results = new ArrayList<>();

	public List<Runnable> getInitList() {
		return initList;
	}

	public List<Supplier<Boolean>> getTerminateConditions() {
		return terminateConditions;
	}

	public List<AbstractDecisionPoint> getDecisionPoints() {
		return decisionPoints;
	}

	public List<Block> getProcessBlocks() {
		return processBlocks;
	}

	public List<AbstractResult<?>> getResults() {
		return results;
	}
}
