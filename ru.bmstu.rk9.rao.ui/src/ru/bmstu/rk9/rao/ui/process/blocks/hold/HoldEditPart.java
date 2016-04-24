package ru.bmstu.rk9.rao.ui.process.blocks.hold;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class HoldEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new HoldFigure();
		return figure;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		if (evt.getPropertyName().equals(HoldNode.PROPERTY_DURATION)) {
			refreshVisuals();
		}
	}
}
