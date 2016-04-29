package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.GridLayer;

public class ProcessGridLayer extends GridLayer {
	public ProcessGridLayer() {
		super();
		setOpaque(false);
	}

	@Override
	protected void paintGrid(Graphics g) {
		Rectangle clip = g.getClip(Rectangle.SINGLETON);
		for (int x = clip.x - clip.x % gridX; x <= clip.x + clip.width; x += gridX) {
			for (int y = clip.y - clip.y % gridY; y <= clip.y + clip.height; y += gridY) {
				g.drawPoint(x, y);
			}
		}
	}
}
