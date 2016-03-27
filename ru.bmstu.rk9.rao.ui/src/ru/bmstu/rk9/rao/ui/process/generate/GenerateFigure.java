package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.ColorConstants;
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
				outputConnectionAnchor.offsetHorizontal = bounds.width - offset;
				outputConnectionAnchor.offsetVertical = bounds.height / 2 + offset;
			}
		});

		label.setText(Generate.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle bounds = getBounds();
		PointList points = new PointList();
		final int centerY = bounds.y + (bounds.height + 2 * offset) / 2;
		final int leftX = bounds.x + offset;
		points.addPoint(leftX, bounds.y + 3 * offset);
		points.addPoint(bounds.x + bounds.width - offset, centerY);
		points.addPoint(leftX, bounds.y + bounds.height - offset);
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPolygon(points);

		paintDock(graphics);

		paintName(bounds);
	}

	private void paintDock(Graphics graphics) {
		Rectangle bounds = getBounds();
		final int dockCenterX = bounds.x + outputConnectionAnchor.offsetHorizontal - 1;
		final int dockCenterY = bounds.y + outputConnectionAnchor.offsetVertical;

		final int dockBackgroundRadius = offset;
		graphics.setBackgroundColor(ColorConstants.white);
		graphics.fillOval(dockCenterX - dockBackgroundRadius, dockCenterY - dockBackgroundRadius,
				dockBackgroundRadius * 2, dockBackgroundRadius * 2);

		final int dockBorderRadius = offset - 1;
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillOval(dockCenterX - dockBorderRadius, dockCenterY - dockBorderRadius, dockBorderRadius * 2,
				dockBorderRadius * 2);
	}
}
