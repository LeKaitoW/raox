package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class QueueEditPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new QueueFigure();
		return figure;
	}
}
