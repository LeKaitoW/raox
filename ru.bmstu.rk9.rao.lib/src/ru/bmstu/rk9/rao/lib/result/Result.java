package ru.bmstu.rk9.rao.lib.result;

import java.util.function.Supplier;

public class Result<T> extends AbstractResult<T> {

	protected Result(Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode,
			Statistics<T> statistics) {
		this.evaluate = evaluate;
		this.condition = condition;
		this.resultMode = resultMode;
		this.statistics = statistics;
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode,
			Statistics<T> statistics) {
		this(evaluate, condition, resultMode, statistics);
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode) {
		this(evaluate, condition, resultMode, getDefaultStatistics(genericClass));
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, ResultMode resultMode, Statistics<T> statistics) {
		this(evaluate, () -> true, resultMode, statistics);
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, Supplier<Boolean> condition, Statistics<T> statistics) {
		this(evaluate, condition, ResultMode.AUTO, statistics);
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, ResultMode resultMode) {
		this(evaluate, () -> true, resultMode, getDefaultStatistics(genericClass));
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, Statistics<T> statistics) {
		this(evaluate, () -> true, ResultMode.AUTO, statistics);
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate, Supplier<Boolean> condition) {
		this(evaluate, condition, ResultMode.AUTO, getDefaultStatistics(genericClass));
	}

	public Result(Class<T> genericClass, Supplier<T> evaluate) {
		this(evaluate, () -> true, ResultMode.AUTO, getDefaultStatistics(genericClass));
	}

	Supplier<T> evaluate;
	Supplier<Boolean> condition;

	@Override
	public T evaluate() {
		return evaluate.get();
	}

	@Override
	public boolean condition() {
		return condition.get();
	}
}
