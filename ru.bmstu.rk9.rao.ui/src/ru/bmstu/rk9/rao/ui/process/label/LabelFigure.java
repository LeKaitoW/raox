package ru.bmstu.rk9.rao.ui.process.label;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;

public class LabelFigure extends Figure {

	@Override
	final protected void paintFigure(Graphics graphics) {
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(getBounds());
	}
}
