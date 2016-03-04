package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class QueueFigure extends ProcessFigure {

	public QueueFigure() {
		super();
		label.setText(Queue.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
