package ru.bmstu.rk9.rao.lib.resource;

import ru.bmstu.rk9.rao.lib.database.Serializable;

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
		this.name = name;
	}

	public String name;

	protected Integer number;
}
