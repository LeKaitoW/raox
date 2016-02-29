package ru.bmstu.rk9.rao.lib.pattern;

import ru.bmstu.rk9.rao.lib.database.Database;

public interface Pattern {
	public static enum ExecutedFrom {
		SEARCH(Database.ResourceEntryType.SEARCH), SOLUTION(Database.ResourceEntryType.SOLUTION);

		public final Database.ResourceEntryType resourceSpecialStatus;

		private ExecutedFrom(Database.ResourceEntryType resourceSpecialStatus) {
			this.resourceSpecialStatus = resourceSpecialStatus;
		}
	}

	public String getName();

	public int[] getRelevantInfo();
}
