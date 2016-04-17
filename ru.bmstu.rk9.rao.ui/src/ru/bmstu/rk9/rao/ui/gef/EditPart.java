package ru.bmstu.rk9.rao.ui.gef;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public abstract class EditPart extends AbstractGraphicalEditPart implements PropertyChangeListener {

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		Node node = (Node) getModel();
		IFigure figure = getFigure();
		figure.getParent().setConstraint(figure, node.getConstraint());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_CONSTRAINT))
			refreshVisuals();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new DeletePolicy());
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
	public void performRequest(Request request) {
		if (request.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
}
