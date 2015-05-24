package ru.bmstu.rk9.rdo.ui.graph;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.PlatformUI;

public class RelativeCoordinates {

	final static Rectangle monitorBounds = PlatformUI.getWorkbench()
			.getDisplay().getBounds();

	public static int setRelX(double relX) {
		return (int) (monitorBounds.width * relX);
	}

	public static int setRelY(double relY) {
		return (int) (monitorBounds.height * relY);
	}
}