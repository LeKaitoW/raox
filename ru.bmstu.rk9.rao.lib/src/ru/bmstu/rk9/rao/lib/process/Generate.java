package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Generate implements Block {

	public Generate(Supplier<Integer> interval) {
		this.interval = interval;
		Simulator.pushEvent(new GenerateEvent(0));
	}

	private Supplier<Integer> interval;
	private boolean ready = false;
	private OutputDock outputDock = new OutputDock();

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public ProcessStatus check() {
		if (!ready)
			return ProcessStatus.NOTHING_TO_DO;

		Transact newTransact = new Transact();
		newTransact.register();
		System.out.println(Simulator.getTime() + ": generate body "
				+ newTransact.getNumber());
		if (!outputDock.pushTransact(newTransact)) {
			Transact.eraseTransact(newTransact);
			System.out.println(Simulator.getTime()
					+ ": generate failed to give " + newTransact.getNumber());
			return ProcessStatus.FAILURE;
		}

		Double time = Simulator.getTime() + interval.get();
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
