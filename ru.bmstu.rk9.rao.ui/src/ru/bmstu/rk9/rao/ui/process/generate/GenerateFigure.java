package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class GenerateFigure extends ProcessFigure {

	public GenerateFigure() {
		super();

		ProcessConnectionAnchor outputConnectionAnchor;
		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Generate.TERMINAL_OUT, outputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				final Rectangle rectangle = figure.getBounds().getCopy();
				outputConnectionAnchor.offsetHorizontal = rectangle.width - offset;
				outputConnectionAnchor.offsetVertical = rectangle.height / 2 + offset;
			}
		});

		label.setText(Generate.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int centerY = rectangle.y + (rectangle.height + 2 * offset) / 2;
		int leftX = rectangle.x + offset;
		points.addPoint(leftX, rectangle.y + 3 * offset);
		points.addPoint(rectangle.x + rectangle.width - offset, centerY);
		points.addPoint(leftX, rectangle.y + rectangle.height - offset);
		graphics.fillPolygon(points);

		paintName(rectangle);
	}
}
