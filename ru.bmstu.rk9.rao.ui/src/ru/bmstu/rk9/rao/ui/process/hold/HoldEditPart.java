package ru.bmstu.rk9.rao.ui.process.hold;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class HoldEditPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new HoldFigure();
		return figure;
	}
}
