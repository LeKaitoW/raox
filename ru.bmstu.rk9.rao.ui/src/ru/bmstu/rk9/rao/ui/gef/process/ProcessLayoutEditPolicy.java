package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.commands.ChangeConstraintCommand;
import ru.bmstu.rk9.rao.ui.gef.commands.CreateCommand;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNodeCreateCommand;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelEditPart;

public class ProcessLayoutEditPolicy extends XYLayoutEditPolicy {

	public static final int FIGURE_WIDTH = 50;
	public static final int FIGURE_HEIGHT = FIGURE_WIDTH;

	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new ProcessEditPolicy();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		if (!ProcessEditor.hasNodeInfo((Class<? extends Node>) child.getModel().getClass()))
			return null;

		return new ChangeConstraintCommand((Node) child.getModel(), (Rectangle) constraint);
	}

	@Override
	protected void showLayoutTargetFeedback(Request request) {
		if (!(request instanceof CreateRequest))
			return;

		CreateRequest createRequest = (CreateRequest) request;
		createRequest.setSize(new Dimension(FIGURE_WIDTH, FIGURE_HEIGHT));
		createRequest.getLocation().performTranslate(-FIGURE_WIDTH / 2, -FIGURE_HEIGHT / 2);
	}

	@Override
	protected IFigure createSizeOnDropFeedback(CreateRequest createRequest) {
		@SuppressWarnings("unchecked")
		NodeInfo nodeInfo = ProcessEditor.getNodeInfo((Class<? extends Node>) createRequest.getNewObjectType());
		BlockNode node = (BlockNode) createRequest.getNewObject();
		ModelNode modelNode = (ModelNode) getHost().getModel();

		IFigure figure = nodeInfo.getFigureFactory().get();
		figure.setForegroundColor(new Color(null, node.getColor()));
		figure.setBackgroundColor(new Color(null, modelNode.getBackgroundColor()));

		ProcessSelectedRectangle processSelectedRectangle = new ProcessSelectedRectangle(figure, modelNode);
		addFeedback(processSelectedRectangle);
		return processSelectedRectangle;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if (request.getType() == REQ_CREATE && getHost() instanceof ProcessModelEditPart) {
			Rectangle constraint = new Rectangle(request.getLocation().x - FIGURE_WIDTH / 2,
					request.getLocation().y - FIGURE_HEIGHT / 2, FIGURE_WIDTH, FIGURE_HEIGHT);
			Object node = request.getNewObject();
			if (node instanceof BlockNode) {
				return new BlockNodeCreateCommand((Node) getHost().getModel(), (BlockNode) node, constraint);
			} else {
				return new CreateCommand((Node) getHost().getModel(), (Node) node, constraint);
			}
		}
		return null;
	}
}
