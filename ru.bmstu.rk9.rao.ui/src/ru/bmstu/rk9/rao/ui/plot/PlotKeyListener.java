package ru.bmstu.rk9.rao.ui.plot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

public class PlotKeyListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		PlotFrame plotFrame2 = (PlotFrame) e.widget;
		if (e.keyCode == SWT.SHIFT) {

			plotFrame2.setRangeZoomable(true);
			System.out.println("Shift");
		} else if (e.keyCode == SWT.CTRL) {
			plotFrame2.setDomainZoomable(true);
			;
			System.out.println("CtRL");
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		PlotFrame plotFrame3 = (PlotFrame) e.widget;
		if (e.keyCode == SWT.SHIFT) {
			plotFrame3.setRangeZoomable(false);
		} else if (e.keyCode == SWT.CTRL) {
			plotFrame3.setDomainZoomable(false);
		}

	}

}
