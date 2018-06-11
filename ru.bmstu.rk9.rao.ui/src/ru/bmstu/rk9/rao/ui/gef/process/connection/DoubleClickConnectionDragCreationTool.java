package ru.bmstu.rk9.rao.ui.gef.process.connection;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.tools.ConnectionDragCreationTool;
import org.eclipse.swt.events.MouseEvent;

public class DoubleClickConnectionDragCreationTool extends ConnectionDragCreationTool {
	@Override
	public void mouseDoubleClick(MouseEvent me, EditPartViewer viewer) {
		super.mouseDown(me, viewer);
	}
}
