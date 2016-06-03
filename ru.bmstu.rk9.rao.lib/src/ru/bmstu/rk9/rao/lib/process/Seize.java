package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.simulator.CurrentSimulator;

public class Seize implements Block {

	public Seize(Resource resource) {
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
		if (resource.isLocked())
			return BlockStatus.NOTHING_TO_DO;
		if (outputDock.hasTransact())
			return BlockStatus.CHECK_AGAIN;

		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		System.out.println(CurrentSimulator.getTime() + ": seize body " + transact.getNumber());
		outputDock.pushTransact(transact);
		resource.lock();
		return BlockStatus.SUCCESS;
	}
}
