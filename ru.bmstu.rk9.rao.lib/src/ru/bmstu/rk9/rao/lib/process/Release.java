package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Release implements BlockWithInput, BlockWithOutput {

	public Release(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;
	private BlockWithInput nextBlock;
	private Transact currentTransact;

	@Override
	public ProcessStatus check() {
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;
		if (!resource.isLocked()) {
			// TODO unrecoverable error?
			return ProcessStatus.FAILURE;
		}

		System.out.println(Simulator.getTime() + ": release body "
				+ currentTransact.getNumber());

		if (!nextBlock.takeTransact(currentTransact)) {
			System.out
					.println(Simulator.getTime() + ": release failed to give "
							+ currentTransact.getNumber());
			return ProcessStatus.FAILURE;
		}

		currentTransact = null;
		resource.unlock();
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
		System.out.println(Simulator.getTime() + ": release take "
				+ transact.getNumber());
		currentTransact = transact;
		return true;
	}

}
