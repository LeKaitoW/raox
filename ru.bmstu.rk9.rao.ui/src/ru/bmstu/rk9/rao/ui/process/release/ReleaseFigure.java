package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Path;

import ru.bmstu.rk9.rao.ui.process.node.BlockFigure;
import ru.bmstu.rk9.rao.ui.process.node.ConnectionAnchor;

public class ReleaseFigure extends BlockFigure {

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

	public ReleaseFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(ReleaseNode.DOCK_IN, inputConnectionAnchor);

		ConnectionAnchor outputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(ReleaseNode.DOCK_OUT, outputConnectionAnchor);

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
				inputConnectionAnchor.setOffsetHorizontal(left - bounds.x);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(bounds.width);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(ReleaseNode.name);
	}

	private static void addArcToPath(Rectangle bounds, Path path) {
		final int startAngle = 240;
		final int arcAngle = 240;
		path.addArc(bounds.x, bounds.y, bounds.width, bounds.height, startAngle, arcAngle);
	}
}
