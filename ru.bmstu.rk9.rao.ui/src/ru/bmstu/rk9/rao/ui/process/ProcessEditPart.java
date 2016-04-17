package ru.bmstu.rk9.rao.ui.process;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.node.BlockNode;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public abstract class ProcessEditPart extends EditPart implements NodeEditPart {

	private int ID;

	public int getID() {
		return ID;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	@Override
	public final List<ru.bmstu.rk9.rao.ui.gef.Node> getModelChildren() {
		return ((ru.bmstu.rk9.rao.ui.gef.Node) getModel()).getChildren();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		if (evt.getPropertyName().equals(BlockNode.PROPERTY_COLOR)) {
			getFigure().setBackgroundColor(new Color(null, (RGB) evt.getNewValue()));
			refreshVisuals();
		}

		if (evt.getPropertyName().equals(BlockNode.PROPERTY_NAME))
			((ProcessFigure) getFigure()).setShowName((boolean) evt.getNewValue());

		if (evt.getPropertyName().equals(BlockNode.SOURCE_CONNECTION_UPDATED))
			refreshSourceConnections();

		if (evt.getPropertyName().equals(BlockNode.TARGET_CONNECTION_UPDATED))
			refreshTargetConnections();
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ConnectionPolicy());
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		if (!(getFigure() instanceof ProcessFigure))
			return;

		ProcessFigure figure = (ProcessFigure) getFigure();
		BlockNode node = (BlockNode) getModel();
		figure.setBackgroundColor(new Color(null, node.getColor()));
		figure.setShowName(node.getShowName());
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
