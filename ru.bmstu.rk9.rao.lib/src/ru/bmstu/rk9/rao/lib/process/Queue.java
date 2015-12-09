package ru.bmstu.rk9.rao.lib.process;

import java.util.LinkedList;
import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;

public class Queue implements Block {

	private InputDock inputDock = new InputDock();
	private OutputDock outputDock = new OutputDock();
	private java.util.Queue<Transact> queue = new LinkedList<Transact>();

	public InputDock getInputDock() {
		return inputDock;
	}

	public OutputDock getOutputDock() {
		return outputDock;
	}

	@Override
	public ProcessStatus check() {
		boolean success = false;
		Transact inputTransact = inputDock.pullTransact();
		if (inputTransact != null) {
			queue.offer(inputTransact);
			success = true;
		}

		Transact outputTransact = queue.peek();
		if (outputTransact != null) {
			if (outputDock.pushTransact(outputTransact)) {
				queue.remove();
				success = true;
			}
		}
		return success ? ProcessStatus.SUCCESS : ProcessStatus.NOTHING_TO_DO;
	}

}
