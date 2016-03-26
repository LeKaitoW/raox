package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class QueueFigure extends ProcessFigure {

	public QueueFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, outputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = offset;
		inputConnectionAnchor.offsetVertical = 35;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_IN, inputConnectionAnchor);

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchor.offsetHorizontal = 45;
		outputConnectionAnchor.offsetVertical = 35;
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Queue.TERMINAL_OUT, outputConnectionAnchor);

		label.setText(Queue.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int xLeft = rectangle.x + offset;
		int xRight = rectangle.x + rectangle.width - offset;
		int yTop = rectangle.y + 3 * offset;
		int yBottom = rectangle.y + rectangle.height - offset;
		
		points.addPoint(xLeft, yTop);
		points.addPoint(xRight, yTop);
		points.addPoint(xRight, yBottom);
		points.addPoint(xLeft, yBottom);
		graphics.fillPolygon(points);
		Color oldColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(ColorConstants.white);

		int width = 6;
		for (int i = 1; i < 4; i++) {
			PointList internalPoints = new PointList();
			internalPoints.addPoint(rectangle.x + i * offset + i * width, rectangle.y + 4 * offset);
			internalPoints.addPoint(rectangle.x + i * offset + (i + 1) * width, rectangle.y + 4 * offset);
			internalPoints.addPoint(rectangle.x + i * offset + (i + 1) * width,
					rectangle.y + rectangle.height - 2 * offset);
			internalPoints.addPoint(rectangle.x + i * offset + i * width, rectangle.y + rectangle.height - 2 * offset);
			graphics.fillPolygon(internalPoints);
		}

		graphics.setBackgroundColor(oldColor);

		paintName(rectangle);
	}
}
