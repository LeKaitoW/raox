package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.tools.PanningSelectionTool;

import ru.bmstu.rk9.rao.ui.gef.NodePropertySource;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;

public class ProcessSelectionTool extends PanningSelectionTool {
	@Override
	protected boolean handleDoubleClick(int button) {
		EditPart editPart = getTargetEditPart();
		if (editPart != null) {
			if (editPart instanceof ScalableRootEditPart)
				NodePropertySource.showPropertiesSheet();
			if (editPart instanceof BlockEditPart) {
				SelectionRequest req = (SelectionRequest) getTargetRequest();
				req.setType(RequestConstants.REQ_OPEN);
				setDragTracker(editPart.getDragTracker(req));
			}
		}
		return super.handleDoubleClick(button);
	}
}
