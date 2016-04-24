package ru.bmstu.rk9.rao.ui.process.node;

import ru.bmstu.rk9.rao.ui.gef.label.LabelNode;

public class BlockTitleNode extends LabelNode {

	private static final long serialVersionUID = 1L;

	private BlockNode blockNode;

	final void attachBlockNode(BlockNode blockNode) {
		this.blockNode = blockNode;
	}

	final void detachBlockNode() {
		blockNode = null;
	}

	@Override
	public void onDelete() {
		blockNode.getParent().removeChild(blockNode);
		blockNode.onDelete();
		blockNode.detachTitle();
		detachBlockNode();
	}
}
