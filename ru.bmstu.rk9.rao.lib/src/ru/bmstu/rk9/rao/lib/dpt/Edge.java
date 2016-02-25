package ru.bmstu.rk9.rao.lib.dpt;

import ru.bmstu.rk9.rao.lib.pattern.Rule;

public abstract class Edge extends AbstractActivity {
	public enum ApplyOrder {
		BEFORE, AFTER
	}

	public Edge(String name, ApplyOrder applyMoment) {
		this.applyOrder = applyMoment;
	}

	public final ApplyOrder applyOrder;

	public abstract double calculateValue();

	public abstract boolean check();
	public abstract Rule execute();
}
