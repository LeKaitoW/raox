package ru.bmstu.rk9.rao.lib.result;

public class Result {

	static public <T> UpdatableResult<T> create(Statistics<T> statistics) {
		return new UpdatableResult<T>(statistics);
	}

	static public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource, ResultMode resultMode,
			Statistics<T> statistics) {
		return new EvaluatableResult<T>(dataSource, resultMode, statistics);
	}

	static public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource, Statistics<T> statistics) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO, statistics);
	}

	static public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource, ResultMode resultMode) {
		return new EvaluatableResult<T>(dataSource, resultMode, dataSource.getDefaultStatistics());
	}

	static public <T> EvaluatableResult<T> create(AbstractDataSource<T> dataSource) {
		return new EvaluatableResult<T>(dataSource, ResultMode.AUTO, dataSource.getDefaultStatistics());
	}
}
