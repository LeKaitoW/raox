package ru.bmstu.rk9.rao.lib.result;

import java.util.LinkedList;
import java.util.List;

public class ResultManager {
	private List<Result<?>> results = new LinkedList<Result<?>>();

	public ResultManager(List<Result<?>> results) {
		this.results.addAll(results);
	}

	public List<Result<?>> getResults() {
		return results;
	}
}
