package ru.bmstu.rk9.rao.ui.process.block.title;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;

public class BlockTitleFigure extends Figure {

	@Override
	final protected void paintFigure(Graphics graphics) {
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(getBounds());
	}
}
