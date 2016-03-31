package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class GenerateFigure extends ProcessFigure {

	class Shape extends Figure
	{
		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds();
			PointList points = new PointList();
			points.addPoint(bounds.x, bounds.y);
			points.addPoint(bounds.x + bounds.width, bounds.y + bounds.height / 2);
			points.addPoint(bounds.x, bounds.y + bounds.height);
			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillPolygon(points);
		}
	}

	private Shape shape = new Shape();

	@Override
	public IFigure getShape()
	{
		return shape;
	}

	public GenerateFigure() {
		super();

		add(shape);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(shape);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Generate.TERMINAL_OUT, outputConnectionAnchor);

		shape.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();
				outputConnectionAnchor.offsetHorizontal = bounds.width + dockSize / 2;
				outputConnectionAnchor.offsetVertical = bounds.height / 2;
			}
		});

		label.setText(Generate.name);
	}
}
