package ru.bmstu.rk9.rao.ui.process.blocks;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.INodeFigure;
import ru.bmstu.rk9.rao.ui.gef.label.LabelFigure;

public class BlockTitleFigure extends LabelFigure implements INodeFigure {

	public BlockTitleFigure() {
		setOpaque(true);
	}

	@Override
	public void assignSettings(IFigure original) {
		BlockTitleFigure originalTitle = (BlockTitleFigure) original;
		setFont(originalTitle.getFont());
		setText(originalTitle.getText());
		setForegroundColor(originalTitle.getForegroundColor());
		setOpaque(originalTitle.isOpaque());
	}
}
