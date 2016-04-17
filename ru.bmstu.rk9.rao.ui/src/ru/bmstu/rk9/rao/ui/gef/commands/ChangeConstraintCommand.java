package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.gef.Node;

public class ChangeConstraintCommand extends ConstraintCommand {

	private Rectangle constraint;
	private Node model;
	private Rectangle previousConstraint;

	@Override
	public void execute() {
		model.setConstraint(constraint);
	}

	@Override
	public void setConstraint(Rectangle constraint) {
		this.constraint = constraint;
	}

	@Override
	public void setModel(Object model) {
		this.model = (Node) model;
		this.previousConstraint = ((Node) model).getConstraint();
	}

	@Override
	public void undo() {
		this.model.setConstraint(this.previousConstraint);
	}
}
