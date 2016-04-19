package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.node.BlockEditPart;

public class GenerateEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new GenerateFigure();
		return figure;
	}
}
