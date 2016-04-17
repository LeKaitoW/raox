package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.gef.Node;

public class CreateCommand extends Command {

	private Node model;
	private Node node;

	public CreateCommand(Node model, Node node, Rectangle constraint) {
		this.model = model;
		this.node = node;
		this.node.setConstraint(constraint);
	}

	@Override
	public void execute() {
		model.addChild(node);
	}

	@Override
	public boolean canUndo() {
		if (model == null || node == null)
			return false;
		return model.contains(node);
	}

	@Override
	public void undo() {
		model.removeChild(node);
	}
}
