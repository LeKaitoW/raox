package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Path;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class SeizeFigure extends ProcessFigure {

	public SeizeFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Seize.TERMINAL_IN, inputConnectionAnchor);

		ProcessConnectionAnchor outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Seize.TERMINAL_OUT, outputConnectionAnchor);

		addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure figure) {
				Path path = new Path(null);
				addArcToPath(path);
				path.close();
				final float xStart = path.getPathData().points[0];

				Rectangle bounds = figure.getBounds();
				inputConnectionAnchor.offsetHorizontal = (int) xStart - bounds.x - 1;
				inputConnectionAnchor.offsetVertical = bounds.height / 2 + offset - 1;

				outputConnectionAnchor.offsetHorizontal = bounds.width - offset - 1;
				outputConnectionAnchor.offsetVertical = inputConnectionAnchor.offsetVertical;
			}
		});

		label.setText(Seize.name);
	}

	@Override
	protected void drawShape(Graphics graphics) {
		Path path = new Path(null);
		addArcToPath(path);
		final float[] points = path.getPathData().points;
		final float xStart = points[0];
		final float yStart = points[1];
		path.lineTo(xStart, yStart);
		path.close();

		graphics.setBackgroundColor(getBackgroundColor());
		graphics.fillPath(path);
	}

	private final void addArcToPath(Path path) {
		Rectangle bounds = getBounds();
		path.addArc(bounds.x + offset, bounds.y + 3 * offset, bounds.width - 2 * offset, bounds.height - 4 * offset,
				240, 240);
	}
}
