package ru.bmstu.rk9.rao.ui.process.label;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.EditPart;

public class LabelPart extends EditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new LabelFigure();
		return figure;
	}
}
