package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;

public class ProcessEditPolicy extends ResizableEditPolicy {

	@Override
	protected IFigure createDragSourceFeedbackFigure() {
		IFigure figure = createFigure(getHost());
		figure.setBounds(getInitialFeedbackBounds());
		addFeedback(figure);
		figure.validate();
		return figure;
	}

	private final IFigure createFigure(EditPart editPart) {
		System.out.println(editPart.toString());
		RoundedRectangle figure = new RoundedRectangle();
		FigureUtilities.makeGhostShape(figure);
		figure.setLineStyle(Graphics.LINE_DOT);
		figure.setForegroundColor(ColorConstants.white);
		return figure;
	}
}
