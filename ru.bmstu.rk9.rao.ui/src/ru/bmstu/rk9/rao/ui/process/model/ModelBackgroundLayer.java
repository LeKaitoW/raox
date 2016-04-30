package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;

public class ModelBackgroundLayer extends Layer {

	@Override
	public void paint(Graphics graphics) {
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(getBounds());
	}
}
