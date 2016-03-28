package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class GenerateFigure extends ProcessFigure {

	private final ProcessConnectionAnchor outputConnectionAnchor;

	public GenerateFigure() {
		super();

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Generate.TERMINAL_OUT, outputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Rectangle bounds = figure.getBounds();
				outputConnectionAnchor.offsetHorizontal = bounds.width - offset - 1;
				outputConnectionAnchor.offsetVertical = bounds.height / 2 + offset - 1;
			}
		});

		label.setText(Generate.name);
	}

	@Override
	protected void drawShape(Graphics graphics) {
		Rectangle bounds = getBounds();
		PointList points = new PointList();
		final int centerY = bounds.y + (bounds.height + 2 * offset) / 2;
		final int leftX = bounds.x + offset;
		points.addPoint(leftX, bounds.y + 3 * offset);
		points.addPoint(bounds.x + bounds.width - offset - dockSize / 2, centerY);
		points.addPoint(leftX, bounds.y + bounds.height - offset);
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPolygon(points);
	}

	@Override
	protected void drawDocks(Graphics graphics) {
		Rectangle bounds = getBounds();
		final int dockCenterX = bounds.x + outputConnectionAnchor.offsetHorizontal;
		final int dockCenterY = bounds.y + outputConnectionAnchor.offsetVertical;
		drawDock(graphics, dockCenterX, dockCenterY);
	}
}
