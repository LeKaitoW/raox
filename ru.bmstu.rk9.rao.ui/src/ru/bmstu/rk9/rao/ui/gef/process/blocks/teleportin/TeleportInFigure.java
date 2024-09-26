package ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportin;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockFigure;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionAnchor;

public class TeleportInFigure extends BlockFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();

			points.addPoint(bounds.x, bounds.y + bounds.height / 3);
			points.addPoint(bounds.x, bounds.y + 2 * bounds.height / 3);
			points.addPoint(bounds.x + bounds.width / 2, bounds.y + 2 * bounds.height / 3);
			points.addPoint(bounds.x + bounds.width / 2, bounds.y + bounds.height);
			points.addPoint(bounds.x + bounds.width, bounds.y + bounds.height / 2);
			points.addPoint(bounds.x + bounds.width / 2, bounds.y);
			points.addPoint(bounds.x + bounds.width / 2, bounds.y + bounds.height / 3);
			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillPolygon(points);

			graphics.fillOval(bounds.x + 5 * bounds.width / 6, bounds.y, bounds.width / 6, bounds.height / 6);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public TeleportInFigure() {
		super(Shape.create());

		ConnectionAnchor inputConnectionAnchor = new ConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(TeleportInNode.DOCK_IN, inputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.setOffsetHorizontal(0);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});
	}
}
