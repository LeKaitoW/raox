package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.INodeFigure;
import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.model.ModelNode;

public class ProcessEditPolicy extends ResizableEditPolicy {

	private class SelectedRectangle extends Figure {

		IFigure figure;
		ModelNode modelNode;

		SelectedRectangle(IFigure figure, ModelNode modelNode) {
			this.figure = figure;
			this.modelNode = modelNode;

			setLayoutManager(new XYLayout());
			add(this.figure);

			addFigureListener(new FigureListener() {
				@Override
				public void figureMoved(IFigure shape) {
					Rectangle shapeBounds = shape.getBounds().getCopy();
					shapeBounds.x = 0;
					shapeBounds.y = 0;
					setConstraint(figure, shapeBounds);
				}
			});
		}

		@Override
		public void paint(Graphics graphics) {

			graphics.setBackgroundColor(new Color(null, invertRgb(modelNode.getBackgroundColor())));
			graphics.setAlpha(50);
			graphics.fillRectangle(getBounds());
			graphics.setAlpha(255);

			figure.paint(graphics);

			Rectangle rectangle = getBounds().getCopy();
			rectangle.width--;
			rectangle.height--;
			final int antialiasPrevious = graphics.getAntialias();
			graphics.setAntialias(SWT.OFF);
			graphics.setLineStyle(Graphics.LINE_DOT);
			graphics.setForegroundColor(new Color(null, invertRgb(modelNode.getBackgroundColor())));
			graphics.drawRectangle(rectangle);
			graphics.setAntialias(antialiasPrevious);
		}

		private final RGB invertRgb(RGB rgb) {
			return new RGB(255 - rgb.red, 255 - rgb.green, 255 - rgb.blue);
		}
	};

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
		return new SelectedRectangle(figure, (ModelNode) ((Node) editPart.getModel()).getRoot());
	}
}
