package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.gef.Node;

public class ChangeConstraintCommand extends Command {

	private Node model;
	private Rectangle constraint;
	private Rectangle previousConstraint;

	public ChangeConstraintCommand(Node model, Rectangle constraint) {
		this.model = model;
		this.previousConstraint = model.getConstraint();
		this.constraint = constraint;
	}

	@Override
	public void execute() {
		model.setConstraint(constraint);
	}

	@Override
	public void undo() {
		this.model.setConstraint(this.previousConstraint);
	}
}
