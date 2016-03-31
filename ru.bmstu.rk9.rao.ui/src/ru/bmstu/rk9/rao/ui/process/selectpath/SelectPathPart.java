package ru.bmstu.rk9.rao.ui.process.selectpath;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class SelectPathPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new SelectPathFigure();
		return figure;
	}

}
