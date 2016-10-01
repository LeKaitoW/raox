package ru.bmstu.rk9.rao.lib.process;

public class Connection {
	public static void linkDocks(OutputDock outputDock, InputDock inputDock) {
		inputDock.addLinkedDock(outputDock);
	}
}
