package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Release implements Block {

	public Release(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;
	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = new OutputDock();

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (outputDock.hasTransact())
			return BlockStatus.CHECK_AGAIN;
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;
		if (!resource.isLocked()) {
			throw new ProcessException(
					"Attempting to release unlocked resource");
		}

		System.out.println(Simulator.getTime() + ": release body "
				+ transact.getNumber());

		outputDock.pushTransact(transact);
		resource.unlock();
		return BlockStatus.SUCCESS;
	}
}
