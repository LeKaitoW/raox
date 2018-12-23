package ru.bmstu.rk9.rao.lib.simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.dpt.AbstractDecisionPoint;
import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.lib.result.AbstractResult;

public class SimulatorInitializationInfo {
	public static final Function<Double, String> DEFAULT_TIME_FORMATTER = (currentTime) -> currentTime.toString();

	/*
	 * TODO Fix bad practice, fields should be private and modified through getters
	 * and setters
	 */
	public final List<Runnable> initList = new ArrayList<>();
	public final List<Supplier<Boolean>> terminateConditions = new ArrayList<>();
	public final List<AbstractDecisionPoint> decisionPoints = new ArrayList<>();
	public final List<Block> processBlocks = new ArrayList<>();
	public final List<AbstractResult<?>> results = new ArrayList<>();

	private Function<Double, String> timeFormatter = DEFAULT_TIME_FORMATTER;

	public Function<Double, String> getTimeFormatter() {
		return timeFormatter;
	}

	public void setTimeFormatter(Function<Double, String> timeFormatter) {
		this.timeFormatter = timeFormatter;
	}

	private double timeStart = 0;

	public double getTimeStart() {
		return timeStart;
	}

	public void setTimeStart(Supplier<Double> timeStart) {
		this.timeStart = timeStart.get();
	}
}
