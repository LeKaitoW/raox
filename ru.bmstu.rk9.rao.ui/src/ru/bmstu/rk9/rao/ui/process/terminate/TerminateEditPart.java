package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class TerminateEditPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TerminateFigure();
		return figure;
	}
}
