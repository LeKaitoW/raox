package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Release implements Block {

	public Release(Resource resource) {
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
		if (transactStorage.hasTransact())
			return BlockStatus.CHECK_AGAIN;
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;
		if (resource.isAccessible()) {
			throw new ProcessException("Attempting to release unlocked resource");
		}

		System.out.println(Simulator.getTime() + ": release body " + transact.getNumber());

		transactStorage.pushTransact(transact);
		resource.setAccessible(true);
		return BlockStatus.SUCCESS;
	}
}
