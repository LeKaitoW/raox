package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Seize implements BlockWithInput, BlockWithOutput {

	public Seize(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;
	private BlockWithInput nextBlock;
	private Transact currentTransact;

	@Override
	public ProcessStatus check() {
		if (resource.isLocked())
			return ProcessStatus.NOTHING_TO_DO;
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": seize body "
				+ currentTransact.getNumber());
		if (!nextBlock.takeTransact(currentTransact)) {
			System.out.println(Simulator.getTime() + ": seize failed to give "
					+ currentTransact.getNumber());
			return ProcessStatus.FAILURE;
		}

		currentTransact = null;
		resource.lock();
		return ProcessStatus.SUCCESS;
	}

	@Override
	public void setNextBlock(BlockWithInput nextBlock) {
		this.nextBlock = nextBlock;
	}

	@Override
	public boolean takeTransact(Transact transact) {
		if (currentTransact != null)
			return false;
		System.out.println(Simulator.getTime() + ": seize take "
				+ transact.getNumber());
		currentTransact = transact;
		return true;
	}

}
