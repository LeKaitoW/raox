package ru.bmstu.rk9.rao.lib.dpt;

import ru.bmstu.rk9.rao.lib.exception.RaoLibException;
import ru.bmstu.rk9.rao.lib.naming.RaoNameable;
import ru.bmstu.rk9.rao.lib.pattern.Pattern;

public abstract class AbstractActivity extends RaoNameable {
	public abstract Pattern getPattern();

	public final Integer getNumber() {
		return number;
	}

	public final void setNumber(Integer number) {
		if (this.number != null)
			throw new RaoLibException("Invalid attempt to set activity number. It is already set to " + number);

		this.number = number;
	}

	protected Integer number = null;
}
