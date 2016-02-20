package ru.bmstu.rk9.rao.lib.pattern;

import java.util.List;

import ru.bmstu.rk9.rao.lib.database.Database;

public abstract class Pattern {
	public static enum ExecutedFrom {
		SEARCH(Database.ResourceEntryType.SEARCH), SOLUTION(Database.ResourceEntryType.SOLUTION);

		public final Database.ResourceEntryType resourceSpecialStatus;

		private ExecutedFrom(Database.ResourceEntryType resourceSpecialStatus) {
			this.resourceSpecialStatus = resourceSpecialStatus;
		}
	}

	public abstract void run();
	public abstract boolean selectRelevantResources();
	public abstract String getName();
	public abstract List<Integer> getRelevantInfo();
}
