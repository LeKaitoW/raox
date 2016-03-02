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

	public final Integer getNumber() {
		return number;
	}

	public final void setName(String name) {
		if (this.name != null)
			throw new RaoLibException("Invalid attempt to set resource name. It is already set to " + name);

		this.name = name;
	}

	public final void setNumber(Integer number) {
		if (this.number != null)
			throw new RaoLibException("Invalid attempt to set resource number. It is already set to " + number);

		this.number = number;
	}

	protected String name = null;
	protected Integer number = null;

	// FIXME
	@Override
	public ByteBuffer serialize() {
		return null;
	}
}
