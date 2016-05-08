package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.commands.ChangeConstraintCommand;
import ru.bmstu.rk9.rao.ui.gef.commands.CreateCommand;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNodeCreateCommand;
import ru.bmstu.rk9.rao.ui.process.model.ProcessModelEditPart;

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
	protected Command getCreateCommand(CreateRequest request) {
		if (request.getType() == REQ_CREATE && getHost() instanceof ProcessModelEditPart) {
			Rectangle constraint = (Rectangle) getConstraintFor(request);
			constraint.x = (constraint.x < 0) ? 0 : constraint.x;
			constraint.y = (constraint.y < 0) ? 0 : constraint.y;
			constraint.width = FIGURE_WIDTH;
			constraint.height = FIGURE_HEIGHT;

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
