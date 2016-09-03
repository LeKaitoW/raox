package ru.bmstu.rk9.rao.lib.dpt;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.pattern.Rule;

public class Edge extends AbstractActivity {
	public enum ApplyOrder {
		BEFORE, AFTER
	}

	public Edge(Supplier<? extends Rule> ruleFabric, double value) {
		this(ruleFabric, () -> value);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, Supplier<Double> value) {
		this(ruleFabric, value, ApplyOrder.AFTER, () -> Double.MAX_VALUE);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, double value, ApplyOrder applyOrder) {
		this(ruleFabric, () -> value, applyOrder);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, Supplier<Double> value, ApplyOrder applyOrder) {
		this(ruleFabric, value, applyOrder, () -> Double.MAX_VALUE);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, double value, ApplyOrder applyOrder, double priority) {
		this(ruleFabric, () -> value, applyOrder, () -> priority);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, double value, ApplyOrder applyOrder, Supplier<Double> priority) {
		this(ruleFabric, () -> value, applyOrder, priority);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, Supplier<Double> value, ApplyOrder applyOrder, double priority) {
		this(ruleFabric, value, applyOrder, () -> priority);
	}

	public Edge(Supplier<? extends Rule> ruleFabric, Supplier<Double> value, ApplyOrder applyOrder,
			Supplier<Double> priority) {
		this.ruleFabric = ruleFabric;
		this.value = value;
		this.applyOrder = applyOrder;
		this.priority = priority;
	}

	public final ApplyOrder applyOrder;
	public final Supplier<Double> priority;
	private final Supplier<Double> value;
	private final Supplier<? extends Rule> ruleFabric;

	public final boolean check() {
		currentRule = ruleFabric.get();

		if (currentRule.selectRelevantResources())
			return true;

		return false;
	}

	public final double calculateValue() {
		return value.get();
	}

	public final Rule execute() {
		currentRule.run();
		return currentRule;
	}

	@Override
	public final Rule getPattern() {
		return currentRule;
	}

	private Rule currentRule = null;
}
