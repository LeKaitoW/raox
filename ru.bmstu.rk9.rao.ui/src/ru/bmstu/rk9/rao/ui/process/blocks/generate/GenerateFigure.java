package ru.bmstu.rk9.rao.ui.process.blocks.generate;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockFigure;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionAnchor;

public class GenerateFigure extends BlockFigure {

	static class Shape extends Figure {
		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();
			points.addPoint(bounds.x, bounds.y);
			points.addPoint(bounds.x + bounds.width, bounds.y + bounds.height / 2);
			points.addPoint(bounds.x, bounds.y + bounds.height);
			graphics.setBackgroundColor(getForegroundColor());
			graphics.fillPolygon(points);
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public GenerateFigure() {
		super(Shape.create());

		ConnectionAnchor outputConnectionAnchor = new ConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(GenerateNode.DOCK_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();
				outputConnectionAnchor.setOffsetHorizontal(bounds.width + dockSize / 2);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});
	}
}
