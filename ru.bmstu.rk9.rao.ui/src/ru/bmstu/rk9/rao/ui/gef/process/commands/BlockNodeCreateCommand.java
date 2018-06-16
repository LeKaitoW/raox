package ru.bmstu.rk9.rao.ui.gef.process.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.commands.CreateCommand;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.TeleportHelper;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportout.TeleportOutNode;

public class BlockNodeCreateCommand extends CreateCommand {

	public BlockNodeCreateCommand(Node model, BlockNode node, Rectangle constraint) {
		super(model, node, constraint);
	}

	@Override
	public void execute() {
		super.execute();

		BlockTitleNode title = new BlockTitleNode();
		BlockNode node = (BlockNode) this.node;
		node.getParent().addChild(title);
		node.attachTitle(title);

		if (node instanceof TeleportOutNode)
			TeleportHelper.nodes.add((TeleportOutNode) node);
	}
}
