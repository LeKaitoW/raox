package ru.bmstu.rk9.rao.ui.gef.process.blocks.teleportout;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;

public class TeleportOutEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new TeleportOutFigure();
		return figure;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case TeleportOutNode.PROPERTY_NAME:
			refreshVisuals();
			break;
		}
	}
}
