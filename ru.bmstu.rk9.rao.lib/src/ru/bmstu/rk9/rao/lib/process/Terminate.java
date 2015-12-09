package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Terminate implements BlockWithInput {

	private Transact currentTransact;

	@Override
	public ProcessStatus check() {
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;
		System.out.println(Simulator.getTime() + ": terminate body "
				+ currentTransact.getNumber());
		Transact.eraseTransact(currentTransact);
		currentTransact = null;
		return ProcessStatus.SUCCESS;
	}

	@Override
	public boolean takeTransact(Transact transact) {
		if (currentTransact != null)
			return false;
		System.out.println(Simulator.getTime() + ": terminate take "
				+ transact.getNumber());
		currentTransact = transact;
		return true;
	}

}
