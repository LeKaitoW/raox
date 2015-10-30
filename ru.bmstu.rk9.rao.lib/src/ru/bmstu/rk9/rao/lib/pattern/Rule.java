package ru.bmstu.rk9.rao.lib.pattern;


public interface Rule extends Pattern {
	public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom, String dptName);
}
