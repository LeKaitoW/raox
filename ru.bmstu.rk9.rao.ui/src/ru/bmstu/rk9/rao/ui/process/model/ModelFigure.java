package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ModelFigure extends Figure {

	private XYLayout layout;

	public ModelFigure() {
		layout = new XYLayout();
		setLayoutManager(layout);
	}

	public void setLayout(Rectangle rect) {
		setBounds(rect);
	}
}
