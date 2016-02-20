package ru.bmstu.rk9.rao.lib.dpt;

public abstract class Activity {
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean check();
}
