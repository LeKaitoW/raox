package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Path;

import ru.bmstu.rk9.rao.ui.process.ConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class SeizeFigure extends ProcessFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Path path = new Path(null);
			addArcToPath(getBounds(), path);
			final float[] points = path.getPathData().points;
			final float xStart = points[0];
			final float yStart = points[1];
			path.lineTo(xStart, yStart);
			path.close();

			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillPath(path);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public SeizeFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(SeizeNode.DOCK_IN, inputConnectionAnchor);

		ConnectionAnchor outputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(SeizeNode.DOCK_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Path path = new Path(null);
				addArcToPath(getShape().getBounds(), path);
				path.close();
				if (path.getPathData().points.length == 0)
					return;
				final int left = (int) path.getPathData().points[0];

				Rectangle bounds = shape.getBounds();
				inputConnectionAnchor.setOffsetHorizontal(0);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(left - bounds.x);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(SeizeNode.name);
	}

	private static void addArcToPath(Rectangle bounds, Path path) {
		final int startAngle = 60;
		final int arcAngle = 240;
		path.addArc(bounds.x, bounds.y, bounds.width, bounds.height, startAngle, arcAngle);
	}
}
