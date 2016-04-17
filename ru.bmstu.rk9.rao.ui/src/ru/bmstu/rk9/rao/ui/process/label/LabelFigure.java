package ru.bmstu.rk9.rao.ui.process.label;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;

public class LabelFigure extends Label {

	@Override
	final protected void paintFigure(Graphics graphics) {
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(getBounds());
		super.paintFigure(graphics);
	}
}
