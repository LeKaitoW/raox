package ru.bmstu.rk9.rao.ui.process.hold;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class HoldFigure extends ProcessFigure {

	private static final Rectangle shapeRectangle = new Rectangle();

	class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			shapeRectangle.x = bounds.x;
			shapeRectangle.y = bounds.y;
			shapeRectangle.width = bounds.width;
			shapeRectangle.height = bounds.height;
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillRectangle(shapeRectangle);

			PointList trianglePoints = new PointList();
			final int offset = Math.min(bounds.width, bounds.height) / 4;
			shapeRectangle.shrink(offset, offset);
			trianglePoints.addPoint(shapeRectangle.x, shapeRectangle.y);
			trianglePoints.addPoint(shapeRectangle.x + shapeRectangle.width, shapeRectangle.y + shapeRectangle.height / 2);
			trianglePoints.addPoint(shapeRectangle.x, shapeRectangle.y + shapeRectangle.height);

			Color previousColor = graphics.getBackgroundColor();
			graphics.setBackgroundColor(pageBackgroundColor);
			graphics.fillPolygon(trianglePoints);
			graphics.setBackgroundColor(previousColor);
		}
	}

	private Shape shape = new Shape();

	@Override
	public IFigure getShape() {
		return shape;
	}

	public HoldFigure() {
		super();

		add(shape);

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(shape);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(shape);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_OUT, outputConnectionAnchor);

		shape.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.offsetHorizontal = 0;
				inputConnectionAnchor.offsetVertical = bounds.height / 2;

				outputConnectionAnchor.offsetHorizontal = bounds.width;
				outputConnectionAnchor.offsetVertical = inputConnectionAnchor.offsetVertical;
			}
		});

		label.setText(Hold.name);
	}
}
