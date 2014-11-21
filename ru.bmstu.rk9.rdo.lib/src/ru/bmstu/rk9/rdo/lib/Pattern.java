package ru.bmstu.rk9.rdo.lib;

public interface Pattern
{
	public static enum ExecutedFrom
	{
		SOME, PRIOR, SEARCH
	}

	public String getName();
	public int[] getRelevantInfo(); 

	public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom);
}
