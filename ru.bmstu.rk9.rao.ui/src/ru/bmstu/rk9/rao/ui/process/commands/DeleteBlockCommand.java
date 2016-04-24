package ru.bmstu.rk9.rao.ui.process.commands;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.commands.CommandDelete;

public class DeleteBlockCommand extends CommandDelete {

	public DeleteBlockCommand(Node node, Node parentNode) {
		super(node, parentNode);
	}

	@Override
	public void execute() {
		super.execute();
	}
}
