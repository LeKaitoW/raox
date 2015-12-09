package ru.bmstu.rk9.rao.lib.process;

import ru.bmstu.rk9.rao.lib.process.Process.ProcessStatus;

public interface Block {
	public ProcessStatus check();
}
