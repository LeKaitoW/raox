package ru.bmstu.rk9.rao.ui.process.terminate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TerminateFigure extends ProcessFigure {

	private Label label = new Label(Terminate.name);

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int centerY = rectangle.y + (rectangle.height - 10) / 2;
		int centerX = rectangle.x + rectangle.width / 2;
		int offsetInner = rectangle.width / 5;
		int offsetOuter = rectangle.width * 2 / 5;

		List<Integer> pointsSequence = new ArrayList<Integer>();
		pointsSequence.add(0);
		pointsSequence.add(offsetInner);
		pointsSequence.add(offsetOuter);
		pointsSequence.add(offsetInner);
		pointsSequence.add(offsetOuter);
		pointsSequence.add(offsetInner);

		for (int i = 0; i < 12; i++) {
			int xSign = i < 6 ? 1 : -1;
			int ySign = i >= 3 && i < 9 ? 1 : -1;
			points.addPoint(centerX + xSign * pointsSequence.get(i % 6),
					centerY + ySign * pointsSequence.get((i + 3) % 6));
		}

		graphics.fillPolygon(points);

		Rectangle relativeRectangle = rectangle.getCopy();
		relativeRectangle.setX(0);
		relativeRectangle.setY(rectangle.height * 2 / 5);

		label.setFont(getFont());
		add(label);
		setConstraint(label, relativeRectangle);
	}
}
