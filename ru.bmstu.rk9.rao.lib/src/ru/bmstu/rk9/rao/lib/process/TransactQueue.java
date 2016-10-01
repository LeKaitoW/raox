package ru.bmstu.rk9.rao.lib.process;

public interface TransactQueue {
	public boolean offer(Transact transact);

	public Transact remove();

	public Transact peek();

	public int size();
}
