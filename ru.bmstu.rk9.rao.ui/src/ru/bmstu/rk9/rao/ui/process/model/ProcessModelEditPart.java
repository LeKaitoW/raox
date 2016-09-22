package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import ru.bmstu.rk9.rao.ui.gef.model.ModelEditPart;
import ru.bmstu.rk9.rao.ui.gef.model.ModelLayer;
import ru.bmstu.rk9.rao.ui.process.ProcessLayoutEditPolicy;

public class ProcessModelEditPart extends ModelEditPart {

	@Override
	protected IFigure createFigure() {
		return new ModelLayer();
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
	}
}
