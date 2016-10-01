package ru.bmstu.rk9.rao.ui.process.blocks.queue;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class QueueEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new QueueFigure();
		return figure;
	}
}
