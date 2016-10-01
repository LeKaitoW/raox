package ru.bmstu.rk9.rao.ui.process.blocks.selectpath;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class SelectPathEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new SelectPathFigure();
		return figure;
	}

}
