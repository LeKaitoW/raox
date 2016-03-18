package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Transact extends ComparableResource<Transact> {
	private Transact() {
	}

	@Override
	public ByteBuffer serialize() {
		return null;
	}

	@Override
	public String getTypeName() {
		return "Transact";
	}

	@Override
	public boolean checkEqual(Transact other) {
		return false;
	}

	public static void eraseTransact(Transact transact) {
		transact.erase();
	}

	public static Transact create() {
		Transact transact = new Transact();
		Simulator.getModelState().addResource(transact);
		return transact;
	}

	@Override
	public void erase() {
		Simulator.getModelState().eraseResource(this);
	}

}
