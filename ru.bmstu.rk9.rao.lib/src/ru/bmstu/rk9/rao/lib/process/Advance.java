package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Advance implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = new OutputDock();
	private int interval = 15;
	private boolean nextBlockIsReady = true;

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public ProcessStatus check() {
		if (!nextBlockIsReady) {
			System.out
					.println(Simulator.getTime() + ": advance failed to give");
			// TODO rollBack
			return ProcessStatus.FAILURE;
		}

		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": advance body "
				+ currentTransact.getNumber());
		Double time = Simulator.getTime() + interval;
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
			nextBlockIsReady = outputDock.pushTransact(transact);
			if (!nextBlockIsReady)
				inputDock.rollBack(transact);
		}
	}
}
