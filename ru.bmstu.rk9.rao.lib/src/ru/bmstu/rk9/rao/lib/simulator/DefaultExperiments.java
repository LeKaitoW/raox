package ru.bmstu.rk9.rao.lib.simulator;

import ru.bmstu.rk9.rao.lib.runtime.RaoRuntime;

public class DefaultExperiments implements Runnable {

	@Override
	public void run() {
		RaoRuntime.start();
	}

}
