package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessDeletePolicy;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionAnchor;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionPolicy;
import ru.bmstu.rk9.rao.ui.gef.process.connection.DoubleClickConnectionDragCreationTool;

public abstract class BlockEditPart extends EditPart implements NodeEditPart {

	private int ID;

	public final int getID() {
		return ID;
	}

	public final void setID(int ID) {
		this.ID = ID;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		BlockTitleNode title;
		switch (evt.getPropertyName()) {
		case Node.PROPERTY_CONSTRAINT:
			title = ((BlockNode) getModel()).getTitle();
			if (title == null)
				break;

			Rectangle previousBlockConstraint = (Rectangle) evt.getOldValue();
			Rectangle currentBlockConstraint = (Rectangle) evt.getNewValue();
			Rectangle titleConstraint = title.getConstraint().getCopy();
			titleConstraint.translate(currentBlockConstraint.x - previousBlockConstraint.x,
					currentBlockConstraint.y - previousBlockConstraint.y);
			title.setConstraint(titleConstraint);
			break;

		case BlockNode.PROPERTY_COLOR:
			getFigure().setForegroundColor(new Color(null, (RGB) evt.getNewValue()));
			refreshVisuals();
			break;

		case BlockNode.PROPERTY_SHOW_NAME:
			title = ((BlockNode) getModel()).getTitle();
			if (title == null)
				break;

			title.setVisible((boolean) evt.getNewValue());
			break;

		case BlockNode.SOURCE_CONNECTION_UPDATED:
			refreshSourceConnections();
			break;

		case BlockNode.TARGET_CONNECTION_UPDATED:
			refreshTargetConnections();
			break;
		}
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnectionPolicy());
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		if (!(getFigure() instanceof BlockFigure))
			return;

		BlockFigure figure = (BlockFigure) getFigure();
		BlockNode node = (BlockNode) getModel();
		figure.setForegroundColor(new Color(null, node.getColor()));

		ModelNode modelNode = (ModelNode) node.getRoot();
		figure.setBackgroundColor(new Color(null, modelNode.getBackgroundColor()));
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

	public boolean isConnected() {
		Node node = (Node) getModel();
		if (node instanceof BlockNode) {
			return ((BlockNode) node).isConnected();
		}
		return false;
	}

	public final String mapConnectionAnchorToDock(ConnectionAnchor connectionAnchor) {
		return getProcessFigure().getConnectionAnchorName(connectionAnchor);
	}

	protected final BlockFigure getProcessFigure() {
		return (BlockFigure) getFigure();
	}

	@Override
	public DragTracker getDragTracker(Request request) {
		if (request.getType().equals(RequestConstants.REQ_OPEN) && !isConnected()) {
			return new DoubleClickConnectionDragCreationTool();
		}
		return super.getDragTracker(request);
	}

}
