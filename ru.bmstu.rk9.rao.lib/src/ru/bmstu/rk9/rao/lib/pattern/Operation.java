package ru.bmstu.rk9.rao.lib.pattern;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

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
		Simulator.pushEvent(new OperationEvent(Simulator.getTime() + duration()));
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
			Simulator.getDatabase().addOperationEndEntry(Operation.this);
			finish();
		}
	}
}
