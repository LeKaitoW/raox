package ru.bmstu.rk9.rao.ui.plot;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;

public class PlotMouseWheelListener implements MouseWheelListener {
	PlotKeyListener Plot = new PlotKeyListener();

	boolean flagX = false, flagY = false;
	int WheelPosition;
	@Override
	public void mouseScrolled(MouseEvent e) {

		// TODO Auto-generated method stub
		System.out.println("Count=" + e.count);
		int WheelPosition = e.count;
send(WheelPosition);

		/*
		 * PlotFrame plotFrame = (PlotFrame) e.widget; if ((e.count > 0) &&
		 * (flagX == true)) { plotFrame.zoomInRange(e.x, e.y); } if ((e.count <
		 * 0) && (flagX == true)) { plotFrame.zoomOutRange(e.x, e.y); }
		 */
	}

	public void send(int WheelPosition) {
		this.WheelPosition=WheelPosition;
	}
	
	public int get() {
		return WheelPosition;
	}
	public boolean getFlagX() {
		return flagX;
	}

	public boolean getFlagY() {
		return flagY;
	}
}

// 1) Определить вперед или назад крутится колесо
// 2) ПОлучить plotframe (e.widjet) -> zoomin and zoomout;
// PlotFrame plotFrame = (PlotFrame) e.widget; ==в методе мауз скроллед
// реализовать паттерн наблюдатель
