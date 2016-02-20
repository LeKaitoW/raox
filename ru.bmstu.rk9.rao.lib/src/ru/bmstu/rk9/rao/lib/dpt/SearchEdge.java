package ru.bmstu.rk9.rao.lib.dpt;

import ru.bmstu.rk9.rao.lib.pattern.Rule;

public abstract class SearchEdge extends Activity {
	public enum ApplyOrder {
		BEFORE, AFTER
	}

	public SearchEdge(String name, ApplyOrder applyMoment) {
		this.applyOrder = applyMoment;
	}

	public final ApplyOrder applyOrder;

	public abstract double calculateValue();
	public abstract Rule execute();
}
