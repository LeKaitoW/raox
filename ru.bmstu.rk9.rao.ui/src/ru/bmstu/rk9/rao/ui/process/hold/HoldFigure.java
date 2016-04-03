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

	static class Shape extends Figure {

		private static final Rectangle shapeRectangle = new Rectangle();

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
			trianglePoints.addPoint(shapeRectangle.x + shapeRectangle.width,
					shapeRectangle.y + shapeRectangle.height / 2);
			trianglePoints.addPoint(shapeRectangle.x, shapeRectangle.y + shapeRectangle.height);

			Color previousColor = graphics.getBackgroundColor();
			graphics.setBackgroundColor(pageBackgroundColor);
			graphics.fillPolygon(trianglePoints);
			graphics.setBackgroundColor(previousColor);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public HoldFigure() {
		super(Shape.create());

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Hold.TERMINAL_OUT, outputConnectionAnchor);

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

		label.setText(Hold.name);
	}
}
