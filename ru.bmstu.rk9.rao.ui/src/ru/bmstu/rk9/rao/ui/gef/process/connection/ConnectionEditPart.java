package ru.bmstu.rk9.rao.ui.gef.process.connection;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

public class ConnectionEditPart extends AbstractConnectionEditPart {

	boolean visible = true;

	@Override
	protected IFigure createFigure() {
		if (visible) {
			return new PolylineConnection();
		} else {
			return new ConnectionEmptyFigure();
		}
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.CONNECTION_ROLE, new ConnectionDeleteEditPolicy());
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
	}

	public void setVisible(boolean value) {
		this.visible = value;
	}
}
