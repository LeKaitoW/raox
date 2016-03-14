package ru.bmstu.rk9.rao.ui.process.test;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TestFigure extends ProcessFigure {

	public TestFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, trueOutputConnectionAnchor, falseOutputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = offset;
		inputConnectionAnchor.offsetVertical = 35;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_IN, inputConnectionAnchor);

		trueOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		trueOutputConnectionAnchor.offsetHorizontal = 45;
		trueOutputConnectionAnchor.offsetVertical = 35;
		outputConnectionAnchors.add(trueOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_TRUE_OUT, trueOutputConnectionAnchor);

		falseOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		falseOutputConnectionAnchor.offsetHorizontal = 25;
		falseOutputConnectionAnchor.offsetVertical = 55;
		outputConnectionAnchors.add(falseOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_FALSE_OUT, falseOutputConnectionAnchor);

		label.setText(Test.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int xCenter = rectangle.x + (rectangle.width) / 2;
		int yCenter = rectangle.y + (rectangle.height + 2 * offset) / 2;
		points.addPoint(xCenter, rectangle.y + 3 * offset);
		points.addPoint(rectangle.x + offset, yCenter);
		points.addPoint(xCenter, rectangle.y + rectangle.height - offset);
		points.addPoint(rectangle.x + rectangle.width - offset, yCenter);
		graphics.fillPolygon(points);

		paintName(rectangle);
	}
}
