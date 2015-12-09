package ru.bmstu.rk9.rao.lib.process;

public class OutputDock {

	private InputDock linkedDock;
	private Transact storedTransact;

	public void setLinkedDock(InputDock inputDock) {
		linkedDock = inputDock;
	}

	public InputDock getLinkedDock() {
		return linkedDock;
	}

	public boolean pushTransact(Transact transact) {
		if (storedTransact != null)
			return false;
		storedTransact = transact;
		return true;
	}

	public Transact pullTransact() {
		Transact transact = storedTransact;
		storedTransact = null;
		return transact;
	}
}
