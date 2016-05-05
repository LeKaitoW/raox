package ru.bmstu.rk9.rao.ui.gef.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;

public class ModelBackgroundLayer extends Layer {

	public static final String MODEL_BACKGROUND_LAYER = "Model Background Layer";

	@Override
	public void paint(Graphics graphics) {
		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillRectangle(getBounds());
	}
}
