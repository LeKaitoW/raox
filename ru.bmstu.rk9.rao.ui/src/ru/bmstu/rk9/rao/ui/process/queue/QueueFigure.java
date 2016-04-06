package ru.bmstu.rk9.rao.ui.process.queue;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class QueueFigure extends ProcessFigure {

	static class Shape extends Figure {

		@Override
		final protected void paintFigure(Graphics graphics) {
			Rectangle bounds = getBounds().getCopy();
			final int border = Math.min(bounds.width, bounds.height) / 7;
			bounds.shrink(0, border);

			graphics.setBackgroundColor(getBackgroundColor());
			graphics.fillRectangle(bounds.x, bounds.y, bounds.width, border);
			graphics.fillRectangle(bounds.x, bounds.bottom() - border, bounds.width, border);

			final int partitionWidth = border;
			int partitionIndex = 0;
			while (true) {
				final int left = bounds.right() - partitionWidth - partitionIndex * partitionWidth * 2;
				if (left < bounds.x + bounds.width / 5)
					break;
				graphics.fillRectangle(left, bounds.y + border, partitionWidth, bounds.height - border * 2);
				++partitionIndex;
			}
		}

		private static IFigure create() {
			return new Shape();
		}
	}

	public QueueFigure() {
		super(Shape.create());

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Queue.DOCK_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(getShape());
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Queue.DOCK_OUT, outputConnectionAnchor);

		getShape().addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure shape) {
				Rectangle bounds = shape.getBounds();

				inputConnectionAnchor.setOffsetHorizontal(0);
				inputConnectionAnchor.setOffsetVertical(bounds.height / 2);

				outputConnectionAnchor.setOffsetHorizontal(bounds.width);
				outputConnectionAnchor.setOffsetVertical(bounds.height / 2);
			}
		});

		label.setText(Queue.name);
	}
}
