package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Advance implements Block {

	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();
	private Supplier<Double> duration;

	public static enum AdvanceAction {
		IN("in"), OUT("out");

		private AdvanceAction(final String action) {
			this.action = action;
		}

		public String getString() {
			return action;
		}

		private final String action;
	}

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

		addAdvanceEntryToDatabase(transact, AdvanceAction.IN);
		Double time = Simulator.getTime() + duration.get();
		Simulator.pushEvent(new AdvanceEvent(transact, time));
		return BlockStatus.SUCCESS;
	}

	private void addAdvanceEntryToDatabase(Transact transact, AdvanceAction advanceAction) {
		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		data.put((byte) advanceAction.ordinal());
		Simulator.getDatabase().addProcessEntry(ProcessEntryType.ADVANCE, transact.getNumber(), data);
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
			addAdvanceEntryToDatabase(transact, AdvanceAction.OUT);
		}
	}
}
