package ru.bmstu.rk9.rao.ui.process.blocks.release;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class ReleaseEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new ReleaseFigure();
		return figure;
	}
}
