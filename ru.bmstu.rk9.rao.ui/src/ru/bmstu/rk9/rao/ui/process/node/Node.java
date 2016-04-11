package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

public class Node extends ru.bmstu.rk9.rao.ui.gef.Node {

	private static final long serialVersionUID = 1;

	public static final String PROPERTY_CONSTRAINT = "NodeConstraint";

	private String name;
	protected RGB color;
	private Rectangle constraint;

	public Node(RGB color) {
		this.name = "Unknown";
		this.color = color;
		this.constraint = new Rectangle(10, 10, 100, 100);
	}

	public RGB getColor() {
		return color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public final void setConstraint(Rectangle constraint) {
		Rectangle previousConstraint = this.constraint;
		this.constraint = constraint;
		getListeners().firePropertyChange(PROPERTY_CONSTRAINT, previousConstraint, constraint);
	}

	public final Rectangle getConstraint() {
		return constraint;
	}
}
