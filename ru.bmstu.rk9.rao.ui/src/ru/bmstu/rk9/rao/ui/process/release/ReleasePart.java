package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class ReleasePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new ReleaseFigure();
		return figure;
	}
}
