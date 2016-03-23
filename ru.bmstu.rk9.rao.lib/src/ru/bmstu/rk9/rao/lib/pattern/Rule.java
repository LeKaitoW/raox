package ru.bmstu.rk9.rao.lib.pattern;

public abstract class Rule extends Pattern {
	// public void addResourceEntriesToDatabase(Pattern.ExecutedFrom
	// executedFrom, String dptName);

	@Override
	public final void run() {
		execute();
		finish();
	}

	protected void execute() {
	}
}
