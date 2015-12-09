package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Advance implements BlockWithInput, BlockWithOutput {

	private Transact currentTransact;
	private BlockWithInput nextBlock;
	private int interval = 15;
	private boolean nextBlockIsReady = true;

	@Override
	public ProcessStatus check() {
		if (!nextBlockIsReady) {
			System.out
					.println(Simulator.getTime() + ": advance failed to give");
			return ProcessStatus.FAILURE;
		}

		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": advance body "
				+ currentTransact.getNumber());
		Double time = Simulator.getTime() + interval;
		Simulator.pushEvent(new AdvanceEvent(currentTransact, time));
		currentTransact = null;
		return ProcessStatus.SUCCESS;

	}

	@Override
	public boolean takeTransact(Transact transact) {
		if (currentTransact != null)
			return false;
		System.out.println(Simulator.getTime() + ": advance take "
				+ transact.getNumber());
		currentTransact = transact;
		return true;
	}

	@Override
	public void setNextBlock(BlockWithInput block) {
		nextBlock = block;
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
			nextBlockIsReady = nextBlock.takeTransact(transact);
		}
	}
}
