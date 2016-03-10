package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class ReleaseFigure extends ProcessFigure {

	public ReleaseFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, outputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = 4;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Release.TERMINAL_IN, inputConnectionAnchor);

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchor.offsetHorizontal = 10;
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Release.TERMINAL_OUT, outputConnectionAnchor);

		label.setText(Release.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
