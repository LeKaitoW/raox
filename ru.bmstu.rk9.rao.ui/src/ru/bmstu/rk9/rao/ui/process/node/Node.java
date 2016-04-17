package ru.bmstu.rk9.rao.ui.process.node;

public class Node extends ru.bmstu.rk9.rao.ui.gef.Node {

	private static final long serialVersionUID = 1;

	public static final String NODE_MARKER = "NodeID";

	protected int ID;
	private String name = "Unknown";

	public final int getID() {
		return ID;
	}

	public final void setID(int ID) {
		this.ID = ID;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}
}
