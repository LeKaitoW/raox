package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class QueueFigure extends ProcessFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();
			final int xLeft = bounds.x;
			final int xRight = bounds.x + bounds.width;
			final int yTop = bounds.y;
			final int yBottom = bounds.y + bounds.height;
			points.addPoint(xLeft, yTop);
			points.addPoint(xRight, yTop);
			points.addPoint(xRight, yBottom);
			points.addPoint(xLeft, yBottom);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillPolygon(points);

			Color previousColor = graphics.getBackgroundColor();
			graphics.setBackgroundColor(pageBackgroundColor);
			final int width = 6;
			final int offset = 5;
			for (int i = 1; i < 4; i++) {
				PointList internalPoints = new PointList();
				internalPoints.addPoint(bounds.x + (i - 1) * offset + i * width, bounds.y + 1 * offset);
				internalPoints.addPoint(bounds.x + (i - 1) * offset + (i + 1) * width, bounds.y + 1 * offset);
				internalPoints.addPoint(bounds.x + (i - 1) * offset + (i + 1) * width,
						bounds.y + bounds.height - 1 * offset);
				internalPoints.addPoint(bounds.x + (i - 1) * offset + i * width, bounds.y + bounds.height - 1 * offset);
				graphics.fillPolygon(internalPoints);
			}
			graphics.setBackgroundColor(previousColor);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public QueueFigure() {
		super(Shape.create());

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.setOffsetHorizontal(0);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(bounds.width);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(Queue.name);
	}
}
