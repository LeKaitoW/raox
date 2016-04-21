package ru.bmstu.rk9.rao.ui.process;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.process.node.BlockNodeFactory;

public class ProcessDropTargetListener extends TemplateTransferDropTargetListener {

	public ProcessDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}

	@Override
	protected CreationFactory getFactory(Object template) {
		if (template instanceof CreationFactory) {
			return ((BlockNodeFactory) template);
		}

		if (template instanceof Class) {
			return new BlockNodeFactory((Class<?>) template);
		}

		return null;
	}
}
