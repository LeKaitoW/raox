package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Terminate implements Block {

	private InputDock inputDock = new InputDock();

	public InputDock getInputDock() {
		return inputDock;
	}

	@Override
	public ProcessStatus check() {
		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;
		System.out.println(Simulator.getTime() + ": terminate body "
				+ currentTransact.getNumber());
		Transact.eraseTransact(currentTransact);
		currentTransact = null;
		return ProcessStatus.SUCCESS;
	}
}
