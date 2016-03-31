package ru.bmstu.rk9.rao.ui.process.hold;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class HoldPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new HoldFigure();
		return figure;
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Hold) getModel()).getChildren();
	}
}
