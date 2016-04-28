package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.swt.SWT;

public class ModelFigure extends GridLayer {

	private boolean showGrid;

	public ModelFigure() {
		setLayoutManager(new XYLayout());
		setOpaque(false);
	}

	public final void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	@Override
	public void paint(Graphics graphics) {
		graphics.setAdvanced(true);
		graphics.setAntialias(SWT.ON);
		graphics.setTextAntialias(SWT.ON);
		super.paint(graphics);
	}

	@Override
	protected void paintGrid(Graphics g) {
		if (!showGrid)
			return;

		Rectangle clip = g.getClip(Rectangle.SINGLETON);
		for (int x = clip.x - clip.x % gridX; x <= clip.x + clip.width; x += gridX) {
			for (int y = clip.y - clip.y % gridY; y <= clip.y + clip.height; y += gridY) {
				g.drawPoint(x, y);
			}
		}
	}
}
