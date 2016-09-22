package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.database.Database.ProcessEntryType;
import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Terminate implements Block {

	private InputDock inputDock = new InputDock();

	public InputDock getInputDock() {
		return inputDock;
	}

	@Override
	public BlockStatus check() {
		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return BlockStatus.NOTHING_TO_DO;

		CurrentSimulator.getDatabase().addProcessEntry(ProcessEntryType.TERMINATE, currentTransact.getNumber(), null);
		Transact.eraseTransact(currentTransact);
		return BlockStatus.SUCCESS;
	}
}
