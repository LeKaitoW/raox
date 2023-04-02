package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class SimulatorInitializationInfo {
	private static final Runnable DEFAULT_EXPERIMENTS = new DefaultExperiments();

	public final List<Runnable> initList = new ArrayList<>();
	public final List<Supplier<Boolean>> terminateConditions = new ArrayList<>();
	public final List<AbstractDecisionPoint> decisionPoints = new ArrayList<>();
	public final List<Block> processBlocks = new ArrayList<>();
	public final List<AbstractResult<?>> results = new ArrayList<>();

	private Runnable experiments;

	public void setExperiments(Runnable experiments) {
		this.experiments = experiments;
	}

	public Runnable getExperiments() {
		if (experiments == null)
			return DEFAULT_EXPERIMENTS;
		return experiments;
	}

}
