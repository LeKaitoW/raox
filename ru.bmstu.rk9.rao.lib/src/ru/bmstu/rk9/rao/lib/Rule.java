package ru.bmstu.rk9.rao.lib;

public interface Rule extends Pattern {
	public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom);
}
