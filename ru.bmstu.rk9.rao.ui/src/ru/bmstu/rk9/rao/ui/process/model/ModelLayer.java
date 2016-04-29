package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;

public class ModelLayer extends Layer {

	public ModelLayer() {
		setLayoutManager(new XYLayout());
		setOpaque(false);
	}

	@Override
	public void paint(Graphics graphics) {
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.setTextAntialias(SWT.ON);
		super.paint(graphics);
	}
}
