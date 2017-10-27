package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class UpdatableResult<T> extends AbstractResult<T> {

	public UpdatableResult(Statistics<T> statistics) {
		super(statistics);
	}

	public final void update(T value) {
		double time = CurrentSimulator.getTime();
		update(value, time);
	};
}
