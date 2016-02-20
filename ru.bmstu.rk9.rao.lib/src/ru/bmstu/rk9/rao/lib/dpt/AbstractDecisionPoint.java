package ru.bmstu.rk9.rao.lib.dpt;

import java.util.function.Supplier;

public abstract class AbstractDecisionPoint {
	protected void init() {
	}

	public abstract boolean check();

	// FIXME
	public String getName() {
		return "";
	}

	public abstract Supplier<Double> getPriority();
}
