package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.gef.Node;

public class CreateCommand extends Command {

	private Node model;
	private Node node;

	public CreateCommand() {
		super();
		model = null;
		node = null;
	}

	public void setNode(Object node) {
		if (node instanceof Node)
			this.node = (Node) node;
	}

	public void setModel(Object model) {
		if (model instanceof Node)
			this.model = (Node) model;
	}

	public void setConstraint(Rectangle constraint) {
		if (node == null)
			return;
		node.setConstraint(constraint);
	}

	@Override
	public boolean canExecute() {
		if (node == null || model == null)
			return false;
		return true;
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
