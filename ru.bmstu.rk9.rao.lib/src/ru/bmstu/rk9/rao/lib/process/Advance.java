package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Advance implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = new OutputDock();
	private Supplier<Integer> duration;
	private Transact temporaryTransactOnOutput;

	public Advance(Supplier<Integer> duration) {
		this.duration = duration;
	}

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (temporaryTransactOnOutput != null) {
			if (outputDock.hasTransact())
				return BlockStatus.CHECK_AGAIN;
			outputDock.pushTransact(temporaryTransactOnOutput);
			temporaryTransactOnOutput = null;
		}

		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		System.out.println(CurrentSimulator.getTime() + ": advance body " + transact.getNumber());
		Double time = CurrentSimulator.getTime() + duration.get();
		CurrentSimulator.pushEvent(new AdvanceEvent(transact, time));
		return BlockStatus.SUCCESS;

	}

	private class AdvanceEvent extends Event {
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
		public void execute() {
			if (temporaryTransactOnOutput != null)
				throw new ProcessException("Transact collision in Advance block");
			System.out.println(CurrentSimulator.getTime() + ": advance run " + transact.getNumber());
			temporaryTransactOnOutput = transact;
		}
	}
}
