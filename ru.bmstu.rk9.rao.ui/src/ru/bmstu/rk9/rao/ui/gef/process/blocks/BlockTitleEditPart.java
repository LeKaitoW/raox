package ru.bmstu.rk9.rao.ui.gef.process.blocks;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import ru.bmstu.rk9.rao.ui.gef.font.Font;
import ru.bmstu.rk9.rao.ui.gef.label.LabelEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.ProcessDeletePolicy;

public class BlockTitleEditPart extends LabelEditPart {

	@Override
	protected IFigure createFigure() {
		return new BlockTitleFigure();
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ProcessDeletePolicy());
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case BlockTitleNode.PROPERTY_FONT:
			Font previousFont = (Font) evt.getOldValue();
			BlockTitleNode node = (BlockTitleNode) getModel();
			Rectangle constraint = node.getConstraint().getCopy();
			Dimension oldDimension = node.getTextBounds(previousFont);
			Dimension newDimension = node.getTextBounds();
			Point delta = node.getTranslation(oldDimension, newDimension);
			constraint.translate(delta);
			node.setConstraint(constraint);
			refreshVisuals();
			break;

		}
	}

}
