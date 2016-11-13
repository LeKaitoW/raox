package ru.bmstu.rk9.rao.ui.plot;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

public class PlotMouseWheelListener implements MouseWheelListener {

	@Override
	public void mouseScrolled(MouseEvent e) {

		PlotFrame plotFrame = (PlotFrame) e.widget;

		if (e.count > 0 && plotFrame.isRangeZoomable()) {
			plotFrame.zoomInRange(e.x, e.y);
		}
		if (e.count < 0 && plotFrame.isRangeZoomable()) {
			plotFrame.zoomOutRange(e.x, e.y);
		}
		if (e.count > 0 && plotFrame.isDomainZoomable()) {
			plotFrame.zoomInDomain(e.x, e.y);
		}
		if (e.count < 0 && plotFrame.isDomainZoomable()) {
			plotFrame.zoomOutDomain(e.x, e.y);
		}
		if (e.count > 0 && plotFrame.isDomainZoomable() && plotFrame.isRangeZoomable()) {
			plotFrame.zoomInBoth(e.x, e.y);
		}
		if (e.count < 0 && plotFrame.isDomainZoomable() && plotFrame.isRangeZoomable()) {
			plotFrame.zoomOutBoth(e.x, e.y);
		}

	}
}