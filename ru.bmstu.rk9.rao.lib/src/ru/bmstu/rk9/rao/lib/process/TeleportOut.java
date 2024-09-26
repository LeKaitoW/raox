package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class TeleportOut extends Block {
	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = (int ID) -> transactStorage.pullTransact();

	public TeleportOut(int ID) {
		super(ID);
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

		Transact transact = inputDock.pullTransact(ID);
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.TELEPORT_OUT, transact.getNumber(), null);
		transactStorage.pushTransact(transact);
		return BlockStatus.SUCCESS;
	}
}
