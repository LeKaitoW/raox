package ru.bmstu.rk9.rao.lib.process;

public interface BlockWithInput extends Block {
	public boolean takeTransact(Transact transact);
}
