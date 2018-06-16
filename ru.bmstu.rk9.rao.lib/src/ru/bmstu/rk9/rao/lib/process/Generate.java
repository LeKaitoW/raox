package ru.bmstu.rk9.rao.lib.process;

import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Generate extends Block {

	public Generate(int ID, Supplier<Double> interval) {
		super(ID);
		this.interval = interval;
		CurrentSimulator.pushEvent(new GenerateEvent(interval.get()));
	}

	private Supplier<Double> interval;
	private boolean ready = false;
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = (int ID) -> transactStorage.pullTransact();

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (!ready)
			return BlockStatus.NOTHING_TO_DO;

		if (transactStorage.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}
		Transact transact = Transact.create();
		transactStorage.pushTransact(transact);
		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.GENERATE, transact.getNumber(), null);

		Double time = CurrentSimulator.getTime() + interval.get();
		CurrentSimulator.pushEvent(new GenerateEvent(time));
		ready = false;
		return BlockStatus.SUCCESS;
	}

	private class GenerateEvent extends Event {
		public GenerateEvent(double time) {
			this.time = time;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void execute() {
			ready = true;
		}
	}
}
