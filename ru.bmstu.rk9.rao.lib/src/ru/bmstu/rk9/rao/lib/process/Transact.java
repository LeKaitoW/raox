package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import java.util.Collection;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceComparison;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class Transact implements Resource, ResourceComparison<Transact> {

	private static ResourceManager<Transact> transactManager = new ResourceManager<>();
	private Integer number = null;
	private static Transact lastCreated;

	@Override
	public ByteBuffer serialize() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getNumber() {
		return number;
	}

	@Override
	public String getTypeName() {
		// TODO !!!!
		return "Transact";
	}

	@Override
	public boolean checkEqual(Transact other) {
		return false;
	}

	public Transact register() {
		this.number = transactManager.getNextNumber();
		transactManager.addResource(this);
		lastCreated = this;
		return this;
	}

	public static Transact getLastCreated() {
		return lastCreated;
	}

	public static Collection<Transact> getAll() {
		return transactManager.getAll();
	}

	public static void eraseTransact(Transact transact) {
		transactManager.eraseResource(transact);
	}

}
