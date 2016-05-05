package ru.bmstu.rk9.rao.ui.process;

import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractEditPart;

import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockEditPart;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockTitleEditPart;
import ru.bmstu.rk9.rao.ui.process.blocks.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionEditPart;
import ru.bmstu.rk9.rao.ui.process.model.ProcessModelEditPart;

public class ProcessEditPartFactory implements EditPartFactory {

	private int currentID = 0;
	private ProcessModelEditPart modelEditPart;

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof Connection) {
			AbstractEditPart editPart = new ConnectionEditPart();
			editPart.setModel(model);
			return editPart;
		}

		Map<Class<?>, NodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		if (!processNodesInfo.containsKey(model.getClass()))
			return null;

		AbstractEditPart editPart = processNodesInfo.get(model.getClass()).getPartFactory().get();
		editPart.setModel(model);

		if (editPart instanceof ProcessModelEditPart) {
			modelEditPart = (ProcessModelEditPart) editPart;
			return editPart;
		}

		if (editPart instanceof BlockEditPart) {
			((BlockEditPart) editPart).setID(currentID);
			((BlockNode) model).setID(currentID);
			currentID++;
		}

		if (editPart instanceof BlockTitleEditPart) {
			((BlockTitleNode) model).setFont(modelEditPart.getFigure().getFont());
		}

		return editPart;
	}
}
