package ru.bmstu.rk9.rao.ui.gef.label;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.EditPart;

public class LabelEditPart extends EditPart {

	private final int border = 5;

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

		figure.setText(node.getText());
		figure.setForegroundColor(new Color(null, node.getTextColor()));
		figure.setBackgroundColor(new Color(null, node.getBackgroundColor()));

		Dimension dimension = FigureUtilities.getStringExtents(figure.getText(), figure.getFont());
		Rectangle constraint = node.getConstraint().getCopy();
		constraint.setWidth(dimension.width() + border * 2);
		constraint.setHeight(dimension.height() + border * 2);
		figure.getParent().setConstraint(figure, constraint);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		if (evt.getPropertyName().equals(LabelNode.PROPERTY_TEXT)
				|| evt.getPropertyName().equals(LabelNode.PROPERTY_TEXT_COLOR)
				|| evt.getPropertyName().equals(LabelNode.PROPERTY_BACKGROUND_COLOR)) {
			refreshVisuals();
		}
	}
}
