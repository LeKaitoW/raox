package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;

public class ModelFigure extends Figure {

	private XYLayout layout;
	private Label label = new Label();

	public ModelFigure() {
		layout = new XYLayout();
		setLayoutManager(layout);
		setForegroundColor(ColorConstants.lightBlue);
		setBorder(new LineBorder(5));
		label.setText("hiii");
		add(label);
		setConstraint(label, new Rectangle(50, 50, 40, 20));
	}

	public void setLayout(Rectangle rect) {
		setBounds(rect);
	}
}
