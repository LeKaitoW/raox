package ru.bmstu.rk9.rao.ui.process.model;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.bmstu.rk9.rao.ui.process.Node;

public class ModelPart extends AbstractGraphicalEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new ModelFigure();
		return figure;
	}

	@Override
	protected void createEditPolicies() {
	}

	protected void refreshVisuals() {
		ModelFigure figure = (ModelFigure) getFigure();
		Model model = (Model) getModel();

		figure.setLayout(model.getLayout());
	}

	public List<Node> getModelChildren() {
		return ((Model) getModel()).getChildrenArray();
	}
}
