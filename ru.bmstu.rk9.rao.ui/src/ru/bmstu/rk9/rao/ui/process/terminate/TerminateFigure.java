package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TerminateFigure extends ProcessFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();
			points.addPoint(bounds.x + bounds.width, bounds.y);
			points.addPoint(bounds.x, bounds.y + bounds.height / 2);
			points.addPoint(bounds.x + bounds.width, bounds.y + bounds.height);
			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillPolygon(points);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public TerminateFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(TerminateNode.DOCK_IN, inputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();
				inputConnectionAnchor.setOffsetHorizontal(-dockSize / 2);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(TerminateNode.name);
	}
}
