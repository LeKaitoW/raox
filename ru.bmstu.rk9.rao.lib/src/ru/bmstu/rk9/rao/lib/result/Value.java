package ru.bmstu.rk9.rao.lib.result;

import java.util.function.Supplier;

public class Value<T> extends Result<T> {

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode, Statistics<T> statistics) {
		this.evaluate = evaluate;
		this.condition = condition;
		this.resultMode = resultMode;
		this.statistics = statistics;
	}

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode) {
		this(evaluate, condition, resultMode, new ValueStatistics<T>());
	}

	public Value(Supplier<T> evaluate, ResultMode resultMode, Statistics<T> statistics) {
		this(evaluate, () -> true, resultMode, statistics);
	}

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition, Statistics<T> statistics) {
		this(evaluate, condition, ResultMode.AUTO, statistics);
	}

	public Value(Supplier<T> evaluate, ResultMode resultMode) {
		this(evaluate, () -> true, resultMode, new ValueStatistics<T>());
	}

	public Value(Supplier<T> evaluate, Statistics<T> statistics) {
		this(evaluate, () -> true, ResultMode.AUTO, statistics);
	}

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition) {
		this(evaluate, condition, ResultMode.AUTO, new ValueStatistics<T>());
	}

	public Value(Supplier<T> evaluate) {
		this(evaluate, () -> true, ResultMode.AUTO, new ValueStatistics<T>());
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
