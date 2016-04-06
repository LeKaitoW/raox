package ru.bmstu.rk9.rao.ui.process.model;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.ScalableRootEditPart;

import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;
import ru.bmstu.rk9.rao.ui.process.ProcessEditor;
import ru.bmstu.rk9.rao.ui.process.ProcessLayoutEditPolicy;

public class ModelPart extends ProcessEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = ((ScalableRootEditPart) getRoot()).getLayer(ProcessEditor.MODEL_LAYER);
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_CONSTRAINT))
			refreshVisuals();
		if (evt.getPropertyName().equals(Node.PROPERTY_ADD))
			refreshChildren();
		if (evt.getPropertyName().equals(Node.PROPERTY_REMOVE))
			refreshChildren();
	}
}
