package ru.bmstu.rk9.rdo.lib;

import java.util.LinkedList;

class ResultManager
{
	private LinkedList<Result> results = new LinkedList<Result>();

	void addResult(Result result)
	{
		results.add(result);
	}

	void printResults()
	{
		for (Result r : results)
			System.out.println(r.get());
	}

	LinkedList<Result> getResults()
	{
		return results;
	}
}
