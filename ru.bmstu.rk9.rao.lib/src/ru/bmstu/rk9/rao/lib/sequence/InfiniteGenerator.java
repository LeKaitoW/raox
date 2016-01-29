package ru.bmstu.rk9.rao.lib.sequence;

import ru.bmstu.rk9.rao.lib.thirdparty.Generator;

public abstract class InfiniteGenerator<T> extends Generator<T> {
	@Override
	protected void run() throws InterruptedException {
		while(true)
			generate();
	}

	protected abstract void generate() throws InterruptedException;
}
