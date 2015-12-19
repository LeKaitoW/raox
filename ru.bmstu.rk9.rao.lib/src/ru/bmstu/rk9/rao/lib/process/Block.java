package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.BlockStatus;

public interface Block {
	public BlockStatus check();
}
