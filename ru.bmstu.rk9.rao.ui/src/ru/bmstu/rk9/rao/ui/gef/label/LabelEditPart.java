package ru.bmstu.rk9.rao.ui.gef.label;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.gef.EditPart;
import ru.bmstu.rk9.rao.ui.gef.alignment.Alignment;
import ru.bmstu.rk9.rao.ui.gef.font.SerializableFontData;

public class LabelEditPart extends EditPart {

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

		figure.setFont(node.getFont().getFont());
		figure.setText(node.getText());
		figure.setForegroundColor(new Color(null, node.getTextColor()));
		figure.setBackgroundColor(new Color(null, node.getBackgroundColor()));
		figure.setVisible(node.getVisible());

		Rectangle constraint = node.getConstraint().getCopy();
		Dimension newDimension = node.getTextBounds();
		constraint.setSize(newDimension);
		figure.getParent().setConstraint(figure, constraint);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case LabelNode.PROPERTY_TEXT:
		case LabelNode.PROPERTY_TEXT_COLOR:
		case LabelNode.PROPERTY_BACKGROUND_COLOR:
		case LabelNode.PROPERTY_VISIBLE:
			refreshVisuals();
			break;
		case LabelNode.PROPERTY_FONT:
			SerializableFontData previousFont = (SerializableFontData) evt.getOldValue();
			LabelNode node = (LabelNode) getModel();
			Alignment alignment = node.getAlignment();
			Rectangle constraint = node.getConstraint().getCopy();
			Dimension oldDimension = node.getTextBounds(previousFont);
			Dimension newDimension = node.getTextBounds();
			Point delta = alignment.translation(oldDimension, newDimension);
			constraint.translate(delta);
			node.setConstraint(constraint);
			refreshVisuals();
			break;

		}
	}
}
