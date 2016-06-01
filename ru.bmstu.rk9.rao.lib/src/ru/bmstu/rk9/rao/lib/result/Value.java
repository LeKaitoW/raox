package ru.bmstu.rk9.rao.lib.result;

import java.util.function.Supplier;

public class Value<T> extends Result<T> {

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition, ResultMode resultMode) {
		this.evaluate = evaluate;
		this.condition = condition;
		this.statistics = new ValueStatistics<>();
		this.resultMode = resultMode;
	}

	public Value(Supplier<T> evaluate, ResultMode resultMode) {
		this(evaluate, () -> true, resultMode);
	}

	public Value(Supplier<T> evaluate, Supplier<Boolean> condition) {
		this(evaluate, condition, ResultMode.AUTO);
	}

	public Value(Supplier<T> evaluate) {
		this(evaluate, () -> true, ResultMode.AUTO);
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
