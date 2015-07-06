package ru.bmstu.rk9.rao.lib.resource;

import ru.bmstu.rk9.rao.lib.database.Serializable;

public interface Resource extends Serializable {
	public String getName();

	public Integer getNumber();

	public String getTypeName();
}
