package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.commands.CreateCommand;

public class BlockNodeCreateCommand extends CreateCommand {

	public BlockNodeCreateCommand(Node model, BlockNode node, Rectangle constraint) {
		super(model, node, constraint);
	}

	@Override
	public void execute() {
		super.execute();

		BlockTitleNode title = new BlockTitleNode();
		node.getParent().addChild(title);
		((BlockNode) node).attachTitle(title);
	}
}
