package ru.bmstu.rk9.rao.ui.process;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

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

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public abstract class ProcessEditPart extends AbstractGraphicalEditPart
		implements PropertyChangeListener, NodeEditPart {

	@Override
	public final List<ru.bmstu.rk9.rao.ui.gef.Node> getModelChildren() {
		return ((ru.bmstu.rk9.rao.ui.gef.Node) getModel()).getChildren();
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_COLOR)) {
			getFigure().setBackgroundColor(new Color(null, (RGB) evt.getNewValue()));
		}
		if (evt.getPropertyName().equals(Node.PROPERTY_NAME)) {
			((ProcessFigure) getFigure()).setFigureNameVisible((boolean) evt.getNewValue());
		}
		if (evt.getPropertyName().equals(Node.PROPERTY_CONSTRAINT))
			refreshVisuals();
		if (evt.getPropertyName().equals(BlockNode.SOURCE_CONNECTION_UPDATED))
			refreshSourceConnections();
		if (evt.getPropertyName().equals(BlockNode.TARGET_CONNECTION_UPDATED))
			refreshTargetConnections();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnectionPolicy());
	}

	@Override
	protected void refreshVisuals() {
		if (!(getFigure() instanceof ProcessFigure))
			return;
		ProcessFigure figure = (ProcessFigure) getFigure();
		Node node = (Node) getModel();

		figure.setConstraint(node.getConstraint());
		RGB oldColor = node.getColor();
		Color newColor = new Color(null, oldColor);
		figure.setBackgroundColor(newColor);
		figure.setFigureNameVisible(node.nameIsVisible());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connectionEditPart) {
		Connection connection = (Connection) connectionEditPart.getModel();
		return getProcessFigure().getConnectionAnchor(connection.getSourceDockName());
	}

	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point point = new Point(((DropRequest) request).getLocation());
		return getProcessFigure().getSourceConnectionAnchorAt(point);
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connectionEditPart) {
		Connection connection = (Connection) connectionEditPart.getModel();
		return getProcessFigure().getConnectionAnchor(connection.getTargetDockName());
	}

	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point point = new Point(((DropRequest) request).getLocation());
		return getProcessFigure().getTargetConnectionAnchorAt(point);
	}

	@Override
	public List<Connection> getModelSourceConnections() {
		Node node = (Node) getModel();
		if (node instanceof BlockNode) {
			return ((BlockNode) node).getSourceConnections();
		}
		return null;
	}

	@Override
	public List<Connection> getModelTargetConnections() {
		Node node = (Node) getModel();
		if (node instanceof BlockNode) {
			return ((BlockNode) node).getTargetConnections();
		}
		return null;
	}

	final protected String mapConnectionAnchorToDock(ConnectionAnchor connectionAnchor) {
		return getProcessFigure().getConnectionAnchorName(connectionAnchor);
	}

	protected ProcessFigure getProcessFigure() {
		return (ProcessFigure) getFigure();
	}
}
