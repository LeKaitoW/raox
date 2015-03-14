package ru.bmstu.rk9.rdo.lib;

public interface Rule extends Pattern {
	public void addResourceEntriesToDatabase(Pattern.ExecutedFrom executedFrom);
}
