package ru.bmstu.rk9.rao.ui.process.test;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.bmstu.rk9.rao.ui.process.ProcessConnectionAnchor;
import ru.bmstu.rk9.rao.ui.process.ProcessFigure;

public class TestFigure extends ProcessFigure {

	public TestFigure() {
		super();

		ProcessConnectionAnchor inputConnectionAnchor, trueOutputConnectionAnchor, falseOutputConnectionAnchor;
		inputConnectionAnchor = new ProcessConnectionAnchor(this);
		inputConnectionAnchor.offsetHorizontal = 4;
		inputConnectionAnchors.add(inputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_IN, inputConnectionAnchor);

		trueOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		trueOutputConnectionAnchor.offsetHorizontal = 10;
		trueOutputConnectionAnchor.offsetVertical = 20;
		outputConnectionAnchors.add(trueOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_TRUE_OUT, trueOutputConnectionAnchor);

		falseOutputConnectionAnchor = new ProcessConnectionAnchor(this);
		falseOutputConnectionAnchor.offsetHorizontal = 50;
		trueOutputConnectionAnchor.offsetVertical = 60;
		outputConnectionAnchors.add(falseOutputConnectionAnchor);
		connectionAnchors.put(Test.TERMINAL_FALSE_OUT, falseOutputConnectionAnchor);

		label.setText(Test.name);
	}

	@Override
	protected void paintFigure(Graphics graphics) {
		Rectangle rectangle = getBounds().getCopy();
		graphics.fillRectangle(rectangle);
	}
}
