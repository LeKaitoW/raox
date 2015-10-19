package ru.bmstu.rk9.rao.ui.process.command;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

public abstract class LayoutCommand extends Command {

	public abstract void setConstraint(Rectangle rectangle);

	public abstract void setModel(Object model);
}
