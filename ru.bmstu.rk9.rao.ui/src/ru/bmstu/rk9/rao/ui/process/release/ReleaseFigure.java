package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class ReleaseFigure extends ProcessFigure {

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
