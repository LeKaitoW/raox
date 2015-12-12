package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Advance implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = new OutputDock();
	private Supplier<Integer> interval;
	private Transact temporaryTransactOnOutput;

	public Advance(Supplier<Integer> interval) {
		this.interval = interval;
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public ProcessStatus check() {
		if (temporaryTransactOnOutput != null) {
			if (!outputDock.pushTransact(temporaryTransactOnOutput))
				return ProcessStatus.FAILURE;
			temporaryTransactOnOutput = null;
		}

		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": advance body "
				+ currentTransact.getNumber());
		Double time = Simulator.getTime() + interval.get();
		Simulator.pushEvent(new AdvanceEvent(currentTransact, time));
		currentTransact = null;
		return ProcessStatus.SUCCESS;

	}

	private class AdvanceEvent implements Event {

		private double time;
		private Transact transact;

		public AdvanceEvent(Transact transact, double time) {
			this.time = time;
			this.transact = transact;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public double getTime() {
			return time;
		}

		@Override
		public void run() {
			System.out.println(Simulator.getTime() + ": advance run "
					+ transact.getNumber());
			temporaryTransactOnOutput = transact;
		}
	}
}
