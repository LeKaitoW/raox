package ru.bmstu.rk9.rao.ui.plot;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

public class PlotMouseWheelListener implements MouseWheelListener {

	boolean flagX = false, flagY = false;

	@Override
	public void mouseScrolled(MouseEvent e) {

		PlotFrame plotFrame = (PlotFrame) e.widget;

		if ((e.count > 0) && (plotFrame.getFlagX() == true)) {
			plotFrame.zoomInRange(e.x, e.y);
		}
		if ((e.count < 0) && (plotFrame.getFlagX() == true)) {
			plotFrame.zoomOutRange(e.x, e.y);
		}
		if ((e.count > 0) && (plotFrame.getFlagY() == true)) {
			plotFrame.zoomInDomain(e.x, e.y);
		}
		if ((e.count < 0) && (plotFrame.getFlagY() == true)) {
			plotFrame.zoomOutDomain(e.x, e.y);
		}
	}

}
