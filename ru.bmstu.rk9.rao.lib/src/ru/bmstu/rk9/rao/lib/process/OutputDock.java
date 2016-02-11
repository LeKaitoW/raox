package ru.bmstu.rk9.rao.lib.process;

public class OutputDock {

	private Transact storedTransact;

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

	public boolean hasTransact() {
		return storedTransact != null;
	}
}
