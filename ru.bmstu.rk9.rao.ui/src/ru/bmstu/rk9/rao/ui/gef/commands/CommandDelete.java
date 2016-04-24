package ru.bmstu.rk9.rao.ui.gef.commands;

import org.eclipse.gef.commands.Command;

import ru.bmstu.rk9.rao.ui.gef.Node;

public class CommandDelete extends Command {

	protected Node node;
	private Node parentNode;

	public CommandDelete(Node node, Node parentNode) {
		this.node = node;
		this.parentNode = parentNode;
	}

	@Override
	public void execute() {
		parentNode.removeChild(node);
		node.deleteCommand();
	}

	@Override
	public void undo() {
		parentNode.addChild(node);
	}
}
