package ru.bmstu.rk9.rao.ui.process;

import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractEditPart;

import ru.bmstu.rk9.rao.ui.gef.NodeInfo;
import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionEditPart;
import ru.bmstu.rk9.rao.ui.process.node.BlockEditPart;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class ProcessEditPartFactory implements EditPartFactory {

	private int currentID = 0;

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractEditPart editPart = null;

		if (model instanceof Connection) {
			editPart = new ConnectionEditPart();
			editPart.setModel(model);
			return editPart;
		}

		Map<Class<?>, NodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		if (!processNodesInfo.containsKey(model.getClass()))
			return null;

		editPart = processNodesInfo.get(model.getClass()).getPartFactory().get();
		editPart.setModel(model);

		if (editPart instanceof BlockEditPart) {
			((BlockEditPart) editPart).setID(currentID);
			((Node) model).setID(currentID);
			currentID++;
		}

		return editPart;
	}
}
