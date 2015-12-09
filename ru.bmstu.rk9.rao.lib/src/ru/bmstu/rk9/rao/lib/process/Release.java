package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;
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
	public ProcessStatus check() {
		Transact currentTransact = inputDock.pullTransact();
		if (currentTransact == null)
			return ProcessStatus.NOTHING_TO_DO;
		if (!resource.isLocked()) {
			// TODO unrecoverable error?
			return ProcessStatus.FAILURE;
		}

		System.out.println(Simulator.getTime() + ": release body "
				+ currentTransact.getNumber());

		if (!outputDock.pushTransact(currentTransact)) {
			System.out
					.println(Simulator.getTime() + ": release failed to give "
							+ currentTransact.getNumber());
			inputDock.rollBack(currentTransact);
			return ProcessStatus.FAILURE;
		}

		currentTransact = null;
		resource.unlock();
		return ProcessStatus.SUCCESS;
	}
}
