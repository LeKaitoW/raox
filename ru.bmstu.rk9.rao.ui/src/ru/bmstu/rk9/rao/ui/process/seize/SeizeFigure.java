package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class SeizeFigure extends ProcessFigure {

	public SeizeFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, outputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = 4;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Seize.TERMINAL_IN, inputConnectionAnchor);

		outputConnectionAnchor = new ProcessConnectionAnchor(this);
		outputConnectionAnchor.offsetHorizontal = 10;
		outputConnectionAnchors.add(outputConnectionAnchor);
		connectionAnchors.put(Seize.TERMINAL_OUT, outputConnectionAnchor);

		label.setText(Seize.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
