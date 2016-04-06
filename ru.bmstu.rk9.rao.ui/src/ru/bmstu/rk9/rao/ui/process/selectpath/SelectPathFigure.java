package ru.bmstu.rk9.rao.ui.process.selectpath;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class SelectPathFigure extends ProcessFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();
			final int xCenter = bounds.x + bounds.width / 2;
			final int yCenter = bounds.y + bounds.height / 2;
			points.addPoint(xCenter, bounds.y);
			points.addPoint(bounds.x + bounds.width, yCenter);
			points.addPoint(xCenter, bounds.y + bounds.height);
			points.addPoint(bounds.x, yCenter);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillPolygon(points);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public SelectPathFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(SelectPathNode.DOCK_IN, inputConnectionAnchor);

		ConnectionAnchor trueOutputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(trueOutputConnectionAnchor);
		connectionAnchors.put(SelectPathNode.DOCK_TRUE_OUT, trueOutputConnectionAnchor);

		ConnectionAnchor falseOutputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(falseOutputConnectionAnchor);
		connectionAnchors.put(SelectPathNode.DOCK_FALSE_OUT, falseOutputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.setOffsetHorizontal(-dockSize / 2);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				trueOutputConnectionAnchor.setOffsetHorizontal(bounds.width + dockSize / 2);
				trueOutputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				falseOutputConnectionAnchor.setOffsetHorizontal(bounds.width / 2);
				falseOutputConnectionAnchor.setOffsetVertical(bounds.height + dockSize / 2);
			}
		});

		label.setText(SelectPathNode.name);
	}
}
