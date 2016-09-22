package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.database.Database.TypeSize;
import ru.bmstu.rk9.rao.lib.event.Event;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Hold implements Block {

	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();
	private Supplier<Double> duration;

	public static enum HoldAction {
		IN("in"), OUT("out");

		private HoldAction(final String action) {
			this.action = action;
		}

		public String getString() {
			return action;
		}

		private final String action;
	}

	public Hold(Supplier<Double> duration) {
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

		addHoldEntryToDatabase(transact, HoldAction.IN);
		Double time = CurrentSimulator.getTime() + duration.get();
		CurrentSimulator.pushEvent(new HoldEvent(transact, time));
		return BlockStatus.SUCCESS;
	}

	private void addHoldEntryToDatabase(Transact transact, HoldAction holdAction) {
		ByteBuffer data = ByteBuffer.allocate(TypeSize.BYTE);
		data.put((byte) holdAction.ordinal());
		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.HOLD, transact.getNumber(), data);
	}

	private class HoldEvent extends Event {
		private Transact transact;

		public HoldEvent(Transact transact, double time) {
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
				throw new ProcessException("Transact collision in Hold block");
			addHoldEntryToDatabase(transact, HoldAction.OUT);
		}
	}
}
