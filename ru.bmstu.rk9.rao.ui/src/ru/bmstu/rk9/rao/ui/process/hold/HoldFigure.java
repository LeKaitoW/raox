package ru.bmstu.rk9.rao.ui.process.hold;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class HoldFigure extends ProcessFigure {

	public HoldFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_OUT, outputConnectionAnchor);

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

		label.setText(Hold.name);
	}

	private static final Rectangle shapeRectangle = new Rectangle();

	@Override
	protected void drawShape(Graphics graphics) {
		Rectangle bounds = getBounds();
		shapeRectangle.x = bounds.x + offset;
		shapeRectangle.y = bounds.y + 3 * offset;
		shapeRectangle.width = bounds.width - 2 * offset;
		shapeRectangle.height = bounds.height - 4 * offset;
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(shapeRectangle);

		PointList trianglePoints = new PointList();
		shapeRectangle.shrink(2 * offset, 2 * offset);
		trianglePoints.addPoint(shapeRectangle.x, shapeRectangle.y);
		trianglePoints.addPoint(shapeRectangle.x + shapeRectangle.width, shapeRectangle.y + shapeRectangle.height / 2);
		trianglePoints.addPoint(shapeRectangle.x, shapeRectangle.y + shapeRectangle.height);

		Color previousColor = graphics.getBackgroundColor();
		graphics.setBackgroundColor(pageBackgroundColor);
		graphics.fillPolygon(trianglePoints);
		graphics.setBackgroundColor(previousColor);
	}
}
