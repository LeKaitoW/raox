package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

public abstract class ConstraintCommand extends Command {

	public abstract void setConstraint(Rectangle constraint);

	public abstract void setModel(Object model);
}
