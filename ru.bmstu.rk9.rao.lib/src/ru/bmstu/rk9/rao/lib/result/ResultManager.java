package ru.bmstu.rk9.rao.lib.result;

import java.util.LinkedList;

public class ResultManager {
	private LinkedList<Result> results = new LinkedList<Result>();

	public void addResult(Result result) {
		results.add(result);
	}

	public LinkedList<Result> getResults() {
		return results;
	}
}
