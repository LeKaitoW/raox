package ru.bmstu.rk9.rao.lib.dpt;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.pattern.Pattern;

public class Activity extends AbstractActivity {
	public Activity(Supplier<? extends Pattern> patternFabric) {
		this(patternFabric, () -> Double.MAX_VALUE);
	}

	public Activity(Supplier<? extends Pattern> patternFabric, double priority) {
		this(patternFabric, () -> priority);
	}

	public Activity(Supplier<? extends Pattern> patternFabric, Supplier<Double> priority) {
		this.patternFabric = patternFabric;
		this.priority = priority;
	}

	public final Supplier<Double> priority;
	private final Supplier<? extends Pattern> patternFabric;

	public final boolean execute() {
		currentPattern = patternFabric.get();

		if (currentPattern.selectRelevantResources()) {
			currentPattern.run();
			return true;
		}

		return false;
	}

	@Override
	public final Pattern getPattern() {
		return currentPattern;
	}

	private Pattern currentPattern = null;
}
