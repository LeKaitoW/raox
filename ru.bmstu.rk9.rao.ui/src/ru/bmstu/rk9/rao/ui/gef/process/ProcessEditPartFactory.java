package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractEditPart;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNode;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockTitleNode;
import ru.bmstu.rk9.rao.ui.gef.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.gef.process.connection.ConnectionEditPart;
import ru.bmstu.rk9.rao.ui.gef.process.model.ProcessModelEditPart;

public class ProcessEditPartFactory implements EditPartFactory {

	private int currentID = 0;
	private ProcessModelEditPart modelEditPart;

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if (model instanceof Connection) {
			Connection connection = (Connection) model;
			ConnectionEditPart editPart = new ConnectionEditPart();
			editPart.setModel(connection);
			editPart.setVisible(connection.isVisible());
			return editPart;
		}

		@SuppressWarnings("unchecked")
		NodeInfo nodeInfo = ProcessEditor.getNodeInfo((Class<? extends Node>) model.getClass());
		if (nodeInfo == null)
			return null;

		AbstractEditPart editPart = nodeInfo.getEditPartFactory().get();
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
