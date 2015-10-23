package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class GenerateFigure extends ProcessFigure {

	private Label label = new Label(Generate.name);

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		PointList points = new PointList();
		int offset = 5;
		int centerY = rectangle.y + (rectangle.height - 10) / 2;
		points.addPoint(rectangle.x + offset, rectangle.y + offset);
		points.addPoint(rectangle.x + rectangle.width - offset, centerY);
		points.addPoint(rectangle.x + offset, rectangle.y + rectangle.height
				- 3 * offset);
		graphics.fillPolygon(points);

		Rectangle relativeRectangle = rectangle.getCopy();
		relativeRectangle.setX(0);
		relativeRectangle.setY(rectangle.height * 2 / 5);

		label.setFont(getFont());
		add(label);
		setConstraint(label, relativeRectangle);
	}
}
