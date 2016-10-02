package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.tools.PanningSelectionTool;

import ru.bmstu.rk9.rao.ui.gef.NodePropertySource;

public class ProcessSelectionTool extends PanningSelectionTool {

	@Override
	protected boolean handleDoubleClick(int button) {
		EditPart editPart = getCurrentViewer().findObjectAtExcluding(getLocation(), getExclusionSet(),
				getTargetingConditional());
		if (editPart instanceof ScalableRootEditPart)
			NodePropertySource.showPropertiesSheet();

		return super.handleDoubleClick(button);
	}
}
