package ru.bmstu.rk9.rao.ui.process.model;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;
import ru.bmstu.rk9.rao.ui.process.ProcessLayoutEditPolicy;

public class ModelPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new ModelFigure();
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
	}

	@Override
	protected void refreshVisuals() {
		ModelFigure figure = (ModelFigure) getFigure();
		Model model = (Model) getModel();

		figure.setLayout(model.getLayout());
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Model) getModel()).getChildren();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_LAYOUT))
			refreshVisuals();
		if (evt.getPropertyName().equals(Node.PROPERTY_ADD))
			refreshChildren();
		if (evt.getPropertyName().equals(Node.PROPERTY_REMOVE))
			refreshChildren();
	}
}
