package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ProcessFigure extends Figure {

	public ProcessFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		setOpaque(true);
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
}
