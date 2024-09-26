package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;

public abstract class Block {
	protected final int ID;

	public abstract BlockStatus check();

	Block(int ID) {
		this.ID = ID;
	}

	public int getID() {
		return ID;
	}
}
