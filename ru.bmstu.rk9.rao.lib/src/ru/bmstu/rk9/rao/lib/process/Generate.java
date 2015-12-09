package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Generate implements BlockWithOutput {

	public Generate() {
		Simulator.pushEvent(new GenerateEvent(0));
	}

	private final int interval = 10;
	private boolean ready = false;
	private BlockWithInput nextBlock;

	@Override
	public void setNextBlock(BlockWithInput block) {
		nextBlock = block;
	}

	@Override
	public ProcessStatus check() {
		if (!ready)
			return ProcessStatus.NOTHING_TO_DO;

		Transact newTransact = new Transact();
		newTransact.register();
		System.out.println(Simulator.getTime() + ": generate body "
				+ newTransact.getNumber());
		if (!nextBlock.takeTransact(newTransact)) {
			// TODO erase resource
			System.out.println(Simulator.getTime()
					+ ": generate failed to give " + newTransact.getNumber());
			return ProcessStatus.FAILURE;
		}

		Double time = Simulator.getTime() + interval;
		Simulator.pushEvent(new GenerateEvent(time));
		ready = false;
		return ProcessStatus.SUCCESS;
	}

	private class GenerateEvent implements Event {

		private double time;

		public GenerateEvent(double time) {
			this.time = time;
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
			ready = true;
		}
	}
}