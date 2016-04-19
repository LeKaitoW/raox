package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.node.BlockEditPart;

public class TerminateEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TerminateFigure();
		return figure;
	}
}
