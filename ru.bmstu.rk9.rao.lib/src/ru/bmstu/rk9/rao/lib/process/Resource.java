package ru.bmstu.rk9.rao.lib.process;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.resource.ComparableResource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;

public class Resource extends ComparableResource<Resource> {
	private Resource() {
	}

	private ResourceState state = ResourceState.UNLOCKED;

	@Override
	public ByteBuffer serialize() {
		return null;
	}

	@Override
	public String getTypeName() {
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

	public static Resource create() {
		Resource resource = new Resource();
		Simulator.getModelState().addResource(resource);
		return resource;
	}

	public boolean isLocked() {
		return state == ResourceState.LOCKED;
	}

	@Override
	public void erase() {
		Simulator.getModelState().eraseResource(this);
	}

	@Override
	public Resource deepCopy() {
		return null;
	}
}
