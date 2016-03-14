package ru.bmstu.rk9.rao.ui.process.advance;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class AdvanceFigure extends ProcessFigure {

	public AdvanceFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, outputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = offset;
		inputConnectionAnchor.offsetVertical = 35;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Advance.TERMINAL_IN, inputConnectionAnchor);

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchor.offsetHorizontal = 45;
		outputConnectionAnchor.offsetVertical = 35;
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Advance.TERMINAL_OUT, outputConnectionAnchor);

		label.setText(Advance.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		points.addPoint(rectangle.x + offset, rectangle.y + 3 * offset);
		points.addPoint(rectangle.x + rectangle.width - offset, rectangle.y + 3 * offset);
		points.addPoint(rectangle.x + rectangle.width - offset, rectangle.y + rectangle.height - offset);
		points.addPoint(rectangle.x + offset, rectangle.y + rectangle.height - offset);
		graphics.fillPolygon(points);

		Color oldColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(ColorConstants.white);
		PointList internalPoints = new PointList();
		internalPoints.addPoint(rectangle.x + 3 * offset, rectangle.y + 5 * offset);
		internalPoints.addPoint(rectangle.x + rectangle.width - 3 * offset,
				rectangle.y + (rectangle.height + 2 * offset) / 2);
		internalPoints.addPoint(rectangle.x + 3 * offset, rectangle.y + rectangle.height - 3 * offset);

		graphics.fillPolygon(internalPoints);
		graphics.setBackgroundColor(oldColor);

		paintName(rectangle);
	}
}
