package ru.bmstu.rk9.rao.lib.pattern;

public abstract class Rule extends Pattern {
	@Override
	public final void run() {
		execute();
		finish();
	}

	protected void execute() {
	}
}
