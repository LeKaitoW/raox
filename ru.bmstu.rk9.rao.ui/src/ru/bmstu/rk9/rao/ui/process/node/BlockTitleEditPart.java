package ru.bmstu.rk9.rao.ui.process.node;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.label.LabelEditPart;

public class BlockTitleEditPart extends LabelEditPart {

	@Override
	protected IFigure createFigure() {
		return new BlockTitleFigure();
	}
}
