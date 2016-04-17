package ru.bmstu.rk9.rao.ui.process.label;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.process.ProcessDeletePolicy;

public class LabelEditPart extends EditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new LabelFigure();
		return figure;
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		LabelNode node = (LabelNode) getModel();
		LabelFigure figure = (LabelFigure) getFigure();

		figure.setText(node.getName());
		figure.setForegroundColor(new Color(null, node.getTextColor()));
		figure.setBackgroundColor(new Color(null, node.getBackgroundColor()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		if (evt.getPropertyName().equals(LabelNode.PROPERTY_TEXT_COLOR)
				|| evt.getPropertyName().equals(LabelNode.PROPERTY_BACKGROUND_COLOR)) {
			refreshVisuals();
		}
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
	}
}
