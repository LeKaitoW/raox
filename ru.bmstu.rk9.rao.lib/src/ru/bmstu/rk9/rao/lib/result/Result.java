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

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, Supplier<Boolean> condition,
			ResultMode resultMode, Statistics<E> statistics) {
		return new Result<E>(evaluate, condition, resultMode, statistics);
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, Supplier<Boolean> condition,
			ResultMode resultMode) {
		return new Result<E>(evaluate, condition, resultMode, getDefaultStatistics(genericClass));
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, ResultMode resultMode,
			Statistics<E> statistics) {
		return new Result<E>(evaluate, () -> true, resultMode, statistics);
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, Supplier<Boolean> condition,
			Statistics<E> statistics) {
		return new Result<E>(evaluate, condition, ResultMode.AUTO, statistics);
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, ResultMode resultMode) {
		return new Result<E>(evaluate, () -> true, resultMode, getDefaultStatistics(genericClass));
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, Statistics<E> statistics) {
		return new Result<E>(evaluate, () -> true, ResultMode.AUTO, statistics);
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate, Supplier<Boolean> condition) {
		return new Result<E>(evaluate, condition, ResultMode.AUTO, getDefaultStatistics(genericClass));
	}

	public static final <E> Result<E> create(Class<E> genericClass, Supplier<E> evaluate) {
		return new Result<E>(evaluate, () -> true, ResultMode.AUTO, getDefaultStatistics(genericClass));
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
