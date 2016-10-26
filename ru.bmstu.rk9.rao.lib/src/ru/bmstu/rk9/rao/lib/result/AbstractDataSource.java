package ru.bmstu.rk9.rao.lib.result;

import java.lang.reflect.ParameterizedType;

public abstract class AbstractDataSource<T> {
	public abstract T evaluate();

	public boolean condition() {
		return true;
	};

	public final Statistics<T> getDefaultStatistics() {
		Class<?> genericClass = (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];

		if (Number.class.isAssignableFrom(genericClass))
			return new WeightedStorelessNumericStatistics();
		else if (Enum.class.isAssignableFrom(genericClass) || String.class.isAssignableFrom(genericClass)
				|| Boolean.class.isAssignableFrom(genericClass))
			return new CategoricalStatistics<T>();
		else
			return new LastValueStatistics<T>();
	}
}
