package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.INodeFigure;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;

public class ProcessEditPolicy extends ResizableEditPolicy {

	ProcessEditPolicy() {
		setResizeDirections(PositionConstants.NONE);
	}

	@Override
	protected IFigure createDragSourceFeedbackFigure() {
		IFigure figure = createFigure((EditPart) getHost());
		figure.setBounds(getInitialFeedbackBounds());
		addFeedback(figure);
		figure.validate();
		return figure;
	}

	private final IFigure createFigure(EditPart editPart) {
		NodeInfo nodeInfo = ProcessEditor.getNodeInfoByEditPart(editPart.getClass());
		IFigure figure = nodeInfo.getFigureFactory().get();
		((INodeFigure) figure).assignSettings(editPart.getFigure());
		figure.setOpaque(false);
		return new ProcessSelectedRectangle(figure, (ModelNode) ((Node) editPart.getModel()).getRoot());
	}
}
