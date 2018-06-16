package ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportin;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;

public class TeleportInEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TeleportInFigure();
		return figure;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case TeleportInNode.PROPERTY_NAME:
		case TeleportInNode.PROPERTY_OUT_NAME:
			refreshVisuals();
			break;
		}
	}
}
