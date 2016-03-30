package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class QueueFigure extends ProcessFigure {

	public QueueFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_OUT, outputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Rectangle bounds = figure.getBounds();

				inputConnectionAnchor.offsetHorizontal = offset - 1;
				inputConnectionAnchor.offsetVertical = bounds.height / 2 + offset - 1;

				outputConnectionAnchor.offsetHorizontal = bounds.width - offset - 1;
				outputConnectionAnchor.offsetVertical = inputConnectionAnchor.offsetVertical;
			}
		});

		label.setText(Queue.name);
	}

	@Override
	protected void drawShape(Graphics graphics) {
		Rectangle bounds = getBounds();
		PointList points = new PointList();
		final int xLeft = bounds.x + offset;
		final int xRight = bounds.x + bounds.width - offset;
		final int yTop = bounds.y + 3 * offset;
		final int yBottom = bounds.y + bounds.height - offset;
		points.addPoint(xLeft, yTop);
		points.addPoint(xRight, yTop);
		points.addPoint(xRight, yBottom);
		points.addPoint(xLeft, yBottom);
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPolygon(points);

		Color previousColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(pageBackgroundColor);
		int width = 6;
		for (int i = 1; i < 4; i++) {
			PointList internalPoints = new PointList();
			internalPoints.addPoint(bounds.x + i * offset + i * width, bounds.y + 4 * offset);
			internalPoints.addPoint(bounds.x + i * offset + (i + 1) * width, bounds.y + 4 * offset);
			internalPoints.addPoint(bounds.x + i * offset + (i + 1) * width, bounds.y + bounds.height - 2 * offset);
			internalPoints.addPoint(bounds.x + i * offset + i * width, bounds.y + bounds.height - 2 * offset);
			graphics.fillPolygon(internalPoints);
		}
		graphics.setBackgroundColor(previousColor);
	}
}
