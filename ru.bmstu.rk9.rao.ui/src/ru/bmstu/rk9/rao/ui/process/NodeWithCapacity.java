package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.swt.graphics.RGB;

public abstract class NodeWithCapacity extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_CAPACITY = "NodeCapacity";

	protected String capacity = "";

	public NodeWithCapacity(RGB backgroundColor) {
		super(backgroundColor);
	}

	public void setCapacity(String capacity) {
		String oldCapacity = this.capacity;
		this.capacity = capacity;
		getListeners().firePropertyChange(PROPERTY_CAPACITY, oldCapacity, capacity);
	}

	public String getCapacity() {
		return capacity;
	}
}
