package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import ru.bmstu.rk9.rao.ui.gef.alignment.Alignment;
import ru.bmstu.rk9.rao.ui.gef.label.LabelNode;

public class BlockTitleNode extends LabelNode {

	private static final long serialVersionUID = 1L;

	private BlockNode blockNode;

	final void attachBlockNode(BlockNode blockNode) {
		this.blockNode = blockNode;
	}

	private final void detachBlockNode() {
		blockNode = null;
	}

	final void cleanup() {
		getParent().removeChild(this);
		detachBlockNode();
	}

	@Override
	public void onDelete() {
		super.onDelete();
		if (blockNode != null)
			blockNode.cleanup();
		cleanup();
	}

	@Override
	protected Alignment getDefaultAlignment() {
		return Alignment.defaultForBlockTitleNode;
	}
}
