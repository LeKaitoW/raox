package ru.bmstu.rk9.rao.ui.process.test;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TestFigure extends ProcessFigure{
	
	public TestFigure() {
		super();
		label.setText(Test.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
