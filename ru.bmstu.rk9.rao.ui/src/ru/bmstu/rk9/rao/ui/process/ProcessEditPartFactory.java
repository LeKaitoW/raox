package ru.bmstu.rk9.rao.ui.process;

import java.util.Map;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

import ru.bmstu.rk9.rao.ui.process.link.LinkPart;
import ru.bmstu.rk9.rao.ui.process.link.Link;

public class ProcessEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;

		if (model instanceof Link) {
			part = new LinkPart();
			part.setModel(model);
			return part;
		}

		Map<Class<?>, ProcessNodeInfo> processNodesInfo = ProcessEditor.processNodesInfo;
		if (!processNodesInfo.containsKey(model.getClass()))
			return null;
		part = processNodesInfo.get(model.getClass()).getPartFactory().get();

		part.setModel(model);
		return part;
	}
}
