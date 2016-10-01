package ru.bmstu.rk9.rao.ui.process.blocks;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import ru.bmstu.rk9.rao.ui.gef.label.LabelEditPart;
import ru.bmstu.rk9.rao.ui.process.ProcessDeletePolicy;

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
}
