package ru.bmstu.rk9.rao.lib.naming;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;

public abstract class RaoNameable {
	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		if (this.name != null)
			throw new RaoLibException("Invalid attempt to set name. It is already set to " + name);

		this.name = name;
	}

	protected String name = null;
}
