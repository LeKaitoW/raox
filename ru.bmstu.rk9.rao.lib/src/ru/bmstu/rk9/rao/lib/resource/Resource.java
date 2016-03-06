package ru.bmstu.rk9.rao.lib.resource;

import java.nio.ByteBuffer;

import ru.bmstu.rk9.rao.lib.database.Serializable;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public abstract class Resource implements Serializable {
	public abstract String getTypeName();

	public abstract void erase();

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		if (this.name != null)
			throw new RaoLibException("Invalid attempt to set resource name. It is already set to " + name);

		this.name = name;
	}

	protected String name = null;

	public final Integer getNumber() {
		return number;
	}

	public final void setNumber(Integer number) {
		if (this.number != null)
			throw new RaoLibException("Invalid attempt to set resource number. It is already set to " + number);

		this.number = number;
	}

	protected Integer number = null;

	public final void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	public final boolean isAccessible() {
		return accessible;
	}

	protected boolean accessible = true;

	// FIXME
	@Override
	public ByteBuffer serialize() {
		return null;
	}
}
