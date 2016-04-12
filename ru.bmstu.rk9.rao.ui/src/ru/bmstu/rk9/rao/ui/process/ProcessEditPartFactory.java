package ru.bmstu.rk9.rao.ui.process;

import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.bmstu.rk9.rao.ui.process.connection.Connection;
import ru.bmstu.rk9.rao.ui.process.connection.ConnectionPart;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class ProcessEditPartFactory implements EditPartFactory {

	private int currentID = 0;

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;

		if (model instanceof Connection) {
			part = new ConnectionPart();
			part.setModel(model);
			return part;
		}

		Map<Class<?>, ProcessNodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		if (!processNodesInfo.containsKey(model.getClass()))
			return null;
		part = processNodesInfo.get(model.getClass()).getPartFactory().get();

		part.setModel(model);
		((ProcessEditPart) part).setID(currentID);
		((Node) model).setID(currentID);
		currentID++;
		return part;
	}
}
