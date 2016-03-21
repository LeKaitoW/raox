package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Seize implements Block {

	public Seize(Resource resource) {
		this.resource = resource;
	}

	private Resource resource;
	private InputDock inputDock = new InputDock();
	private TransactStorage transactStorage = new TransactStorage();
	private OutputDock outputDock = () -> transactStorage.pullTransact();

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public BlockStatus check() {
		if (!resource.isAccessible())
			return BlockStatus.NOTHING_TO_DO;
		if (transactStorage.hasTransact())
			return BlockStatus.CHECK_AGAIN;

		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": seize body " + transact.getNumber());
		transactStorage.pushTransact(transact);
		resource.setAccessible(false);
		return BlockStatus.SUCCESS;
	}
}
