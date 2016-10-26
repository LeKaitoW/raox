package ru.bmstu.rk9.rao.lib.resource;

import ru.bmstu.rk9.rao.lib.database.Serializable;
import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;

public abstract class Resource extends RaoNameable implements Serializable {
	public abstract String getTypeName();

	public abstract void erase();

	public final Integer getNumber() {
		return number;
	}

	public final void setNumber(Integer number) {
		if (this.number != null)
			throw new RaoLibException("Invalid attempt to set resource number. It is already set to " + number);

		this.number = number;
	}

	protected Integer number = null;

	public final void take() {
		refcounter++;
	}

	public final void put() {
		refcounter--;
	}

	public final boolean isAccessible() {
		return refcounter == 0;
	}

	protected int refcounter = 0;

	public final boolean isShallowCopy() {
		return isShallowCopy;
	}

	protected boolean isShallowCopy = false;
}
