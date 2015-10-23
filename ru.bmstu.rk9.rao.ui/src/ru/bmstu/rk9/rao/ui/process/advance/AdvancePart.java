package ru.bmstu.rk9.rao.ui.process.advance;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class AdvancePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new AdvanceFigure();
		return figure;
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Advance) getModel()).getChildren();
	}
}
