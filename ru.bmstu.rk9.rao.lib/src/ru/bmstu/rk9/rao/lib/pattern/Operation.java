package ru.bmstu.rk9.rao.lib.pattern;

import java.util.ArrayList;
import java.util.List;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Operation extends Pattern {
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
			return Operation.this.getName() + "_endEvent";
		}

		@Override
		public void run() {
			Operation.this.end();
		}
	}

	@Override
	public boolean selectRelevantResources() {
		return true;
	}

	@Override
	public String getName() {
		return "Nameless operation";
	}

	@Override
	public List<Integer> getRelevantInfo() {
		return new ArrayList<>();
	}
}
