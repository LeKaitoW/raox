package ru.bmstu.rk9.rao.ui.process.terminate;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class TerminatePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TerminateFigure();
		return figure;
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Terminate) getModel()).getChildren();
	}
}
