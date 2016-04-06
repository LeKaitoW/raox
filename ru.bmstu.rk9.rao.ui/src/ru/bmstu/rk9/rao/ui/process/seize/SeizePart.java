package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class SeizePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new SeizeFigure();
		return figure;
	}
}
