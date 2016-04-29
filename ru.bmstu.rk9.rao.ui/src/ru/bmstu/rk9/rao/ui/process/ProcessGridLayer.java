package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.ScalableRootEditPart;

public class ProcessGridLayer extends GridLayer {

	private boolean showGrid;

	public ProcessGridLayer() {
		setOpaque(false);
	}

	public static ProcessGridLayer getProcessGridLayer(EditPart editPart) {
		LayeredPane printableLayer = (LayeredPane) ((ScalableRootEditPart) editPart.getRoot())
				.getLayer(LayerConstants.PRINTABLE_LAYERS);
		return (ProcessGridLayer) printableLayer.getLayer(LayerConstants.GRID_LAYER);
	}

	public final void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
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
