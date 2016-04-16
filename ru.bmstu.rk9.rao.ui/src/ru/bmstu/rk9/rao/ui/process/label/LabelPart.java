package ru.bmstu.rk9.rao.ui.process.label;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.EditPart;

public class LabelPart extends EditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new LabelFigure();
		return figure;
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();

		LabelNode node = (LabelNode) getModel();
		LabelFigure figure = (LabelFigure) getFigure();
		figure.setText(node.getName());
		figure.setForegroundColor(new Color(null, node.getColor()));
	}
}
