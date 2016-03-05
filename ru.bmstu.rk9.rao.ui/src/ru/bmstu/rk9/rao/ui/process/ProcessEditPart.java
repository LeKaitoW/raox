package ru.bmstu.rk9.rao.ui.process;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.bmstu.rk9.rao.ui.process.link.ProcessLink;

public abstract class ProcessEditPart extends AbstractGraphicalEditPart
		implements PropertyChangeListener, NodeEditPart {

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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(NodeWithProperty.PROPERTY_COLOR)) {
			getFigure().setBackgroundColor(new Color(null, (RGB) evt.getNewValue()));
		}
		if (evt.getPropertyName().equals(NodeWithProperty.PROPERTY_NAME)) {
			((ProcessFigure) getFigure()).setFigureNameVisible((boolean) evt.getNewValue());
		}
		if (evt.getPropertyName().equals(Node.PROPERTY_LAYOUT))
			refreshVisuals();
		if (evt.getPropertyName().equals(Node.SOURCE_CONNECTION))
			refreshSourceConnections();
		if (evt.getPropertyName().equals(Node.TARGET_CONNECTION))
			refreshTargetConnections();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ProcessConnectionPolicy());
	}

	@Override
	protected void refreshVisuals() {
		ProcessFigure figure = (ProcessFigure) getFigure();
		Node model = (Node) getModel();

		figure.setLayout(model.getLayout());
		RGB oldColor = model.getColor();
		Color newColor = new Color(null, oldColor);
		figure.setBackgroundColor(newColor);

		if (model instanceof NodeWithProperty)
			figure.setFigureNameVisible(((NodeWithProperty) model).nameIsVisible());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		ProcessLink processLink = (ProcessLink) connection.getModel();
		return getProcessFigure().getConnectionAnchor(processLink.getSourceTerminal());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point point = new Point(((DropRequest) request).getLocation());
		return getProcessFigure().getSourceConnectionAnchorAt(point);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		ProcessLink processLink = (ProcessLink) connection.getModel();
		return getProcessFigure().getConnectionAnchor(processLink.getTargetTerminal());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point point = new Point(((DropRequest) request).getLocation());
		return getProcessFigure().getTargetConnectionAnchorAt(point);
	}

	@Override
	public List<ProcessLink> getModelSourceConnections() {
		return ((Node) getModel()).getSourceConnectionsArray();
	}

	@Override
	public List<ProcessLink> getModelTargetConnections() {
		return ((Node) getModel()).getTargetConnectionsArray();
	}

	final protected String mapConnectionAnchorToTerminal(ConnectionAnchor connectionAnchor) {
		return getProcessFigure().getConnectionAnchorName(connectionAnchor);
	}

	protected ProcessFigure getProcessFigure() {
		return (ProcessFigure) getFigure();
	}
}
