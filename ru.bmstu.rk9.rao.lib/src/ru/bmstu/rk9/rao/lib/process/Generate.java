package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Generate implements Block {

	public Generate(Supplier<Integer> interval) {
		this.interval = interval;
		Simulator.pushEvent(new GenerateEvent(interval.get()));
	}

	private Supplier<Integer> interval;
	private boolean ready = false;
	private OutputDock outputDock = new OutputDock();

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (!ready)
			return BlockStatus.NOTHING_TO_DO;

		if (outputDock.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}
		Transact transact = new Transact();
		transact.register();
		outputDock.pushTransact(transact);
		System.out.println(Simulator.getTime() + ": generate body "
				+ transact.getNumber());

		Double time = Simulator.getTime() + interval.get();
		Simulator.pushEvent(new GenerateEvent(time));
		ready = false;
		return BlockStatus.SUCCESS;
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
