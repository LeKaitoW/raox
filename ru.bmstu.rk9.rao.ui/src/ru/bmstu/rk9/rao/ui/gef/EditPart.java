package ru.bmstu.rk9.rao.ui.gef;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.process.ProcessDeletePolicy;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public abstract class EditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		IFigure figure = getFigure();
		Node node = (Node) getModel();

		figure.getParent().setConstraint(figure, node.getConstraint());

		RGB oldColor = node.getColor();
		Color newColor = new Color(null, oldColor);
		figure.setBackgroundColor(newColor);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_CONSTRAINT))
			refreshVisuals();

		if (evt.getPropertyName().equals(Node.PROPERTY_COLOR))
			getFigure().setBackgroundColor(new Color(null, (RGB) evt.getNewValue()));
	}

	@Override
	public void activate() {
		super.activate();
		((Node) getModel()).addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		super.deactivate();
		((Node) getModel()).removePropertyChangeListener(this);
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
	}
}
