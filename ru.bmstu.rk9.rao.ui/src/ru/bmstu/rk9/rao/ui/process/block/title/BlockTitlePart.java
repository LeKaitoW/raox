package ru.bmstu.rk9.rao.ui.process.block.title;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.EditPart;

public class BlockTitlePart extends EditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new BlockTitleFigure();
		return figure;
	}
}
