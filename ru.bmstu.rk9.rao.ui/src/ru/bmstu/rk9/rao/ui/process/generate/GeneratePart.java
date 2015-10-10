package ru.bmstu.rk9.rao.ui.process.generate;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.bmstu.rk9.rao.ui.process.Node;

public class GeneratePart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new GenerateFigure();
		return figure;
	}

	@Override
	protected void createEditPolicies() {
	}

	protected void refreshVisuals() {
		GenerateFigure figure = (GenerateFigure) getFigure();
		Generate model = (Generate) getModel();

		figure.setLayout(model.getLayout());
	}

	public List<Node> getModelChildren() {
		return ((Generate) getModel()).getChildrenArray();
	}
}
