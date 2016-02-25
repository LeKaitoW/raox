package ru.bmstu.rk9.rao.lib.pattern;

import java.util.ArrayList;
import java.util.List;

public abstract class Rule extends Pattern {
	// public void addResourceEntriesToDatabase(Pattern.ExecutedFrom
	// executedFrom, String dptName);

	@Override
	public final void run() {
		execute();
	}

	protected void execute() {
	}

	@Override
	public String getName() {
		return "Nameless rule";
	}

	@Override
	public List<Integer> getRelevantInfo() {
		return new ArrayList<>();
	}
}
