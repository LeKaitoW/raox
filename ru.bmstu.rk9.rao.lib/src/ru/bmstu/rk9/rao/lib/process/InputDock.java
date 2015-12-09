package ru.bmstu.rk9.rao.lib.process;

public class InputDock {

	private OutputDock linkedDock;

	public void setLinkedDock(OutputDock outputDock) {
		linkedDock = outputDock;
	}

	public OutputDock getLinkedDock() {
		return linkedDock;
	}

	public Transact pullTransact() {
		return linkedDock.pullTransact();
	}

	public void rollBack(Transact transact) {
		linkedDock.pushTransact(transact);
	}
}
