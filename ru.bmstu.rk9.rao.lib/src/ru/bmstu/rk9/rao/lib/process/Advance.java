package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Advance implements Block {

	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();
	private Supplier<Double> duration;

	public Advance(Supplier<Double> duration) {
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
		if (transactStorage.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}

		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": advance body " + transact.getNumber());
		Double time = Simulator.getTime() + duration.get();
		Simulator.pushEvent(new AdvanceEvent(transact, time));
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
			if (!transactStorage.pushTransact(transact))
				throw new ProcessException("Transact collision in Advance block");
			System.out.println(Simulator.getTime() + ": advance run " + transact.getNumber());
		}
	}
}
