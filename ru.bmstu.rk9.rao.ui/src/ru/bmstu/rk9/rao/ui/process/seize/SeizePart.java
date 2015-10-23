package ru.bmstu.rk9.rao.ui.process.seize;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class SeizePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new SeizeFigure();
		return figure;
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Seize) getModel()).getChildren();
	}
}
