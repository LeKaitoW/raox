package ru.bmstu.rk9.rao.ui.process.test;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TestFigure extends ProcessFigure {

	public TestFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor trueOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(trueOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_TRUE_OUT, trueOutputConnectionAnchor);

		ProcessConnectionAnchor falseOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(falseOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_FALSE_OUT, falseOutputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Rectangle bounds = figure.getBounds();

				inputConnectionAnchor.offsetHorizontal = offset - 1;
				inputConnectionAnchor.offsetVertical = bounds.height / 2 + offset - 1;

				trueOutputConnectionAnchor.offsetHorizontal = bounds.width - offset - 1;
				trueOutputConnectionAnchor.offsetVertical = inputConnectionAnchor.offsetVertical;

				falseOutputConnectionAnchor.offsetHorizontal = bounds.width / 2 - 1;
				falseOutputConnectionAnchor.offsetVertical = bounds.height - offset - 1;
			}
		});

		label.setText(Test.name);
	}

	@Override
	protected void drawShape(Graphics graphics) {
		Rectangle bounds = getBounds();
		PointList points = new PointList();
		final int xCenter = bounds.x + (bounds.width) / 2;
		final int yCenter = bounds.y + (bounds.height + 2 * offset) / 2;
		points.addPoint(xCenter, bounds.y + 3 * offset + dockSize / 2);
		points.addPoint(bounds.x + bounds.width - offset - dockSize / 2, yCenter);
		points.addPoint(xCenter, bounds.y + bounds.height - offset - dockSize / 2);
		points.addPoint(bounds.x + offset + dockSize / 2, yCenter);
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPolygon(points);
	}
}
