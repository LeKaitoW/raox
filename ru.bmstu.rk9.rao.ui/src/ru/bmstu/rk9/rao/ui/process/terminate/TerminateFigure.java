package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TerminateFigure extends ProcessFigure {

	private final ProcessConnectionAnchor inputConnectionAnchor;

	public TerminateFigure() {
		super();

		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Terminate.TERMINAL_IN, inputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Rectangle bounds = figure.getBounds();
				inputConnectionAnchor.offsetHorizontal = offset - 1;
				inputConnectionAnchor.offsetVertical = bounds.height / 2 + offset - 1;
			}
		});

		label.setText(Terminate.name);
	}

	@Override
	protected void drawShape(Graphics graphics) {
		Rectangle bounds = getBounds();
		final int centerY = bounds.y + (bounds.height + 2 * offset) / 2;
		final int xRight = bounds.x + bounds.width - offset;

		PointList points = new PointList();
		points.addPoint(xRight, bounds.y + 3 * offset);
		points.addPoint(bounds.x + offset + dockSize / 2, centerY);
		points.addPoint(xRight, bounds.y + bounds.height - offset);

		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPolygon(points);
	}

	@Override
	protected void drawDocks(Graphics graphics) {
		Rectangle bounds = getBounds();
		final int dockCenterX = bounds.x + inputConnectionAnchor.offsetHorizontal;
		final int dockCenterY = bounds.y + inputConnectionAnchor.offsetVertical;
		drawDock(graphics, dockCenterX, dockCenterY);
	}
}
