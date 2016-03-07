package ru.bmstu.rk9.rao.lib.dpt;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.naming.RaoNameable;

public abstract class AbstractDecisionPoint extends RaoNameable {
	protected void init() {
	}

	public abstract boolean check();

	public abstract Supplier<Double> getPriority();
}
