package ru.bmstu.rk9.rao.ui.process.terminate;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import ru.bmstu.rk9.rao.ui.process.Node;
import ru.bmstu.rk9.rao.ui.process.ProcessDeletePolicy;
import ru.bmstu.rk9.rao.ui.process.ProcessEditPart;

public class TerminatePart extends ProcessEditPart {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_LAYOUT))
			refreshVisuals();
	}

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TerminateFigure();
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
	}

	@Override
	public List<Node> getModelChildren() {
		return ((Terminate) getModel()).getChildren();
	}

	@Override
	protected void refreshVisuals() {
		TerminateFigure figure = (TerminateFigure) getFigure();
		Terminate model = (Terminate) getModel();

		figure.setLayout(model.getLayout());
	}
}
