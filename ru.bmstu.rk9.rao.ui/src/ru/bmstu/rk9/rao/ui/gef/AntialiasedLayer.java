package ru.bmstu.rk9.rao.ui.gef;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.swt.SWT;

public class AntialiasedLayer extends Layer {

	public static void setAntialias(Graphics graphics) {
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.setTextAntialias(SWT.ON);
	}

	@Override
	public void paint(Graphics graphics) {
		setAntialias(graphics);
		super.paint(graphics);
	}
}
