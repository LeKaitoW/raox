package ru.bmstu.rk9.rao.lib.pattern;

import ru.bmstu.rk9.rao.lib.database.SerializationConstants;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public abstract class Operation extends Pattern {
	@Override
	public final void run() {
		begin();
		planEnd();
	}

	protected void begin() {
	}

	protected void end() {
	}

	protected double duration() {
		return 0;
	}

	private final void planEnd() {
		CurrentSimulator.pushEvent(new OperationEvent(CurrentSimulator.getTime() + duration()));
	}

	private class OperationEvent extends Event {
		OperationEvent(double time) {
			this.time = time;
		}

		@Override
		public String getName() {
			return Operation.this.getTypeName() + "_endEvent";
		}

		@Override
		protected void execute() {
			Operation.this.end();
			CurrentSimulator.getDatabase().addOperationEndEntry(Operation.this);
			CurrentSimulator.getDatabase().addMemorizedResourceEntries(
					Operation.this.getTypeName() + "." + SerializationConstants.CREATED_RESOURCES, null, null);
			finish();
		}
	}
}
