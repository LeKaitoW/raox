package ru.bmstu.rk9.rao.lib.result;

import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class EvaluatableResult<T> extends AbstractResult<T> {

	public EvaluatableResult(AbstractDataSource<T> dataSource, ResultMode resultMode, Statistics<T> statistics) {
		super(statistics);
		this.dataSource = dataSource;
		this.resultMode = resultMode;
	}

	public final void update() {
		if (!dataSource.condition())
			return;
		final T value = dataSource.evaluate();
		double time = CurrentSimulator.getTime();
		update(value, time);
		CurrentSimulator.getDatabase().addResultEntry(this, value);
	};

	private final ResultMode resultMode;

	private final AbstractDataSource<T> dataSource;

	public final ResultMode getResultMode() {
		return resultMode;
	}
}
