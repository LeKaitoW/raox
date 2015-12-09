package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;

public class Test implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock trueOutputDock = new OutputDock();
	private OutputDock falseOutputDock = new OutputDock();

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getTrueOutputDock() {
		return trueOutputDock;
	}

	public OutputDock getFalseOutputDock() {
		return falseOutputDock;
	}

	@Override
	public ProcessStatus check() {
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return ProcessStatus.NOTHING_TO_DO;

		OutputDock outputDock;
		if (transact.getNumber() % 2 == 0)
			outputDock = trueOutputDock;
		else
			outputDock = falseOutputDock;

		if (!outputDock.pushTransact(transact)) {
			inputDock.rollBack(transact);
			return ProcessStatus.FAILURE;
		}

		return ProcessStatus.SUCCESS;
	}

}
