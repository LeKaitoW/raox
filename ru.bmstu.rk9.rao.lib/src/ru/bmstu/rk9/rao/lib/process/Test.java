package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;

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
	public BlockStatus check() {
		Transact transact = inputDock.pullTransact();
		if (transact == null)
			return BlockStatus.NOTHING_TO_DO;

		OutputDock outputDock;
		if (transact.getNumber() % 2 == 0)
			outputDock = trueOutputDock;
		else
			outputDock = falseOutputDock;

		if (outputDock.hasTransact()) {
			return BlockStatus.CHECK_AGAIN;
		}

		outputDock.pushTransact(transact);

		return BlockStatus.SUCCESS;
	}

}
