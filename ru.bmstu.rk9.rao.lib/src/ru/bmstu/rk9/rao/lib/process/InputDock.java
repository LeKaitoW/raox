package ru.bmstu.rk9.rao.lib.process;

import java.util.LinkedList;
import java.util.List;

public class InputDock {

	private List<OutputDock> linkedDocks = new LinkedList<OutputDock>();

	public void addLinkedDock(OutputDock outputDock) {
		linkedDocks.add(outputDock);
	}

	public Transact pullTransact(int ID) {
		for (OutputDock linkedDock : linkedDocks) {
			Transact transact = linkedDock.apply(ID);
			if (transact != null)
				return transact;
		}
		return null;
	}
}
