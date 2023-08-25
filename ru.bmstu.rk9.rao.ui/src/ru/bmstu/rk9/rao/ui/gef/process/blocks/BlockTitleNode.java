package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

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

	public Point getTranslation(Dimension oldDimension, Dimension newDimension) {
		return new Point(oldDimension.width / 2 - newDimension.width / 2, oldDimension.height - newDimension.height);
	}
}
