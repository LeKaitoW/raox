package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TerminateFigure extends ProcessFigure {

	public TerminateFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = offset;
		inputConnectionAnchor.offsetVertical = 35;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Terminate.TERMINAL_IN, inputConnectionAnchor);

		label.setText(Terminate.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int centerY = rectangle.y + (rectangle.height + 2 * offset) / 2;
		int xRight = rectangle.x + rectangle.width - offset;

		points.addPoint(xRight, rectangle.y + 3 * offset);
		points.addPoint(rectangle.x + offset, centerY);
		points.addPoint(xRight, rectangle.y + rectangle.height - offset);
		graphics.fillPolygon(points);

		paintName(rectangle);
	}
}
