package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;
import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.resource.ResourceComparison;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class Transact implements Resource, ResourceComparison<Transact> {

	private static ResourceManager<Transact> transactManager = new ResourceManager<>();
	private Integer number = null;

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
		if (number == null)
			throw new ProcessException("Transact not registered");
		return number;
	}

	@Override
	public String getTypeName() {
		return "Transact";
	}

	@Override
	public boolean checkEqual(Transact other) {
		return false;
	}

	public void register() {
		this.number = transactManager.getNextNumber();
		transactManager.addResource(this);
	}

	public static void eraseTransact(Transact transact) {
		transactManager.eraseResource(transact);
	}

}
