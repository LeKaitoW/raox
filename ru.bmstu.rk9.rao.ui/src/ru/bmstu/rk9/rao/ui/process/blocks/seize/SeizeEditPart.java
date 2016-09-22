package ru.bmstu.rk9.rao.ui.process.blocks.seize;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class SeizeEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new SeizeFigure();
		return figure;
	}
}
