package ru.bmstu.rk9.rao.lib.process;

import java.util.Collection;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class TeleportIn extends Block {

	private InputDock inputDock = new InputDock();
	private TeleportTransactStorage transactStorage;
	private OutputDock outputDock = (int ID) -> transactStorage.pullTransact(ID);

	public TeleportIn(int ID, Collection<Integer> linkedIds) {
		super(ID);
		transactStorage = new TeleportTransactStorage(linkedIds);
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

		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.TELEPORT_IN, transact.getNumber(), null);
		transactStorage.pushTransact(transact);
		return BlockStatus.SUCCESS;
	}
}
