package ru.bmstu.rk9.rao.ui.process.test;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class TestPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TestFigure();
		return figure;
	}

}
