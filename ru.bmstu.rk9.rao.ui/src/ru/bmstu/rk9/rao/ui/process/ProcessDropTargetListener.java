package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.process.node.NodeFactory;

public class ProcessDropTargetListener extends TemplateTransferDropTargetListener {

	public ProcessDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}

	@Override
	protected CreationFactory getFactory(Object template) {
		if (template instanceof CreationFactory) {
			return ((NodeFactory) template);
		}

		if (template instanceof Class) {
			return new NodeFactory((Class<?>) template);
		}

		return null;
	}
}
