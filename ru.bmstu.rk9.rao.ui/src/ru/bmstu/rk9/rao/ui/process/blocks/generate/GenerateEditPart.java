package ru.bmstu.rk9.rao.ui.process.blocks.generate;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;

import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;

public class GenerateEditPart extends BlockEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new GenerateFigure();
		return figure;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		switch (evt.getPropertyName()) {
		case GenerateNode.PROPERTY_INTERVAL:
			refreshVisuals();
			break;
		}
	}
}
