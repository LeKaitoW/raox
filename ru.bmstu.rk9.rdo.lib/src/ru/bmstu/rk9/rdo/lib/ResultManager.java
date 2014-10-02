package ru.bmstu.rk9.rdo.lib;

import java.util.LinkedList;

class ResultManager
{
	private LinkedList<Result> results = new LinkedList<Result>();

	void addResult(Result result)
	{
		results.add(result);
	}

	LinkedList<Result> getResults()
	{
		return results;
	}
}
