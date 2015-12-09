package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

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
	public ProcessStatus check() {
		if (resource.isLocked())
			return ProcessStatus.NOTHING_TO_DO;
		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;

		System.out.println(Simulator.getTime() + ": seize body "
				+ currentTransact.getNumber());
		if (!outputDock.pushTransact(currentTransact)) {
			System.out.println(Simulator.getTime() + ": seize failed to give "
					+ currentTransact.getNumber());
			inputDock.rollBack(currentTransact);
			return ProcessStatus.FAILURE;
		}

		currentTransact = null;
		resource.lock();
		return ProcessStatus.SUCCESS;
	}
}
