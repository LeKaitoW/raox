package ru.bmstu.rk9.rao.ui.process.resource;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class ResourcePart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new ResourceFigure();
		return figure;
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Resource) getModel()).getChildren();
	}
}
