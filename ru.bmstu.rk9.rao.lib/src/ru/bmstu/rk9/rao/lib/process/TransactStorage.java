package ru.bmstu.rk9.rao.lib.process;

public class TransactStorage {
	protected Transact currentTransact;

	public Transact pullTransact() {
		if (currentTransact == null)
			return null;

		Transact transact = currentTransact;
		currentTransact = null;
		return transact;
	}

	public boolean pushTransact(Transact transact) {
		if (currentTransact != null)
			return false;

		this.currentTransact = transact;
		return true;
	}

	public boolean hasTransact() {
		return currentTransact != null;
	}
}
