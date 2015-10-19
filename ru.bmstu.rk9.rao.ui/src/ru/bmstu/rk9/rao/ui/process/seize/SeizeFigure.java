package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class SeizeFigure extends Figure {

	public SeizeFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);

		setForegroundColor(ColorConstants.cyan);
		setBackgroundColor(ColorConstants.red);
		setBorder(new LineBorder(1));
		setOpaque(true);
	}

	public void setLayout(Rectangle rect) {
		getParent().setConstraint(this, rect);
	}
}
