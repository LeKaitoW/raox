package ru.bmstu.rk9.rao.ui.process.advance;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class AdvanceFigure extends Figure {

	public AdvanceFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
}
