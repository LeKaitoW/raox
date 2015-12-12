package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.resource.ResourceComparison;
import ru.bmstu.rk9.rao.lib.resource.ResourceManager;

public class Resource implements ru.bmstu.rk9.rao.lib.resource.Resource,
		ResourceComparison<Resource> {

	private static ResourceManager<Resource> resourceManager = new ResourceManager<>();
	private ResourceState state = ResourceState.UNLOCKED;
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
		return number;
	}

	@Override
	public String getTypeName() {
		// TODO !!!!
		return "Resource";
	}

	public void lock() {
		state = ResourceState.LOCKED;
	}

	public void unlock() {
		state = ResourceState.UNLOCKED;
	}

	public enum ResourceState {
		LOCKED, UNLOCKED
	}

	@Override
	public boolean checkEqual(Resource other) {
		return false;
	}

	public Resource register() {
		this.number = resourceManager.getNextNumber();
		resourceManager.addResource(this);
		return this;
	}

	public boolean isLocked() {
		return state == ResourceState.LOCKED;

	}
}
