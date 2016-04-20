package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.gef.commands.CreateCommand;
import ru.bmstu.rk9.rao.ui.gef.label.LabelNode;

public class BlockNodeCreateCommand extends CreateCommand {

	public BlockNodeCreateCommand(ru.bmstu.rk9.rao.ui.gef.Node model, BlockNode node, Rectangle constraint) {
		super(model, node, constraint);
	}

	@Override
	public void execute() {
		super.execute();
		((BlockNode) node).setTitle(new LabelNode());
	}
}
