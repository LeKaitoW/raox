package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.INodeFigure;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;

public class ProcessEditPolicy extends ResizableEditPolicy {

	private class Shape extends RectangleFigure {

		IFigure figure;

		Shape(IFigure figure) {
			this.figure = figure;

			FigureUtilities.makeGhostShape(this);
			setLineStyle(Graphics.LINE_DOT);
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
		protected void fillShape(Graphics graphics) {
			super.fillShape(graphics);
			figure.paint(graphics);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			graphics.setForegroundColor(ColorConstants.white);
			super.outlineShape(graphics);
		}
	};

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
		Shape shape = new Shape(figure);
		return shape;
	}
}
