package ru.bmstu.rk9.rao.lib.event;

import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public abstract class Event {
	protected double time;

	public final double getTime() {
		return time;
	}

	public abstract String getName();

	public final void run() {
		execute();
		CurrentSimulator.getDatabase().addEventEntry(this);
		CurrentSimulator.getDatabase().addMemorizedResourceEntries(
				this.getName() + "." + SerializationConstants.CREATED_RESOURCES, null, null);
	}

	protected abstract void execute();
}
