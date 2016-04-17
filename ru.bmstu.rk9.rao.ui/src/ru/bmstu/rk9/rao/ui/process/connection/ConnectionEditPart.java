package ru.bmstu.rk9.rao.ui.process.connection;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

import ru.bmstu.rk9.rao.ui.process.node.ConnectionDeleteEditPolicy;

public class ConnectionEditPart extends AbstractConnectionEditPart {

	@Override
	protected IFigure createFigure() {
		PolylineConnection connection = new PolylineConnection();
		return connection;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionDeleteEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
	}
}
