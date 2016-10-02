package ru.bmstu.rk9.rao.ui.gef.process;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import ru.bmstu.rk9.rao.ui.gef.Node;
import ru.bmstu.rk9.rao.ui.gef.process.blocks.BlockNodeFactory;

public class ProcessDropTargetListener extends TemplateTransferDropTargetListener {

	public ProcessDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CreationFactory getFactory(Object template) {
		if (template instanceof CreationFactory) {
			return ((BlockNodeFactory) template);
		}

		if (template instanceof Node) {
			return new BlockNodeFactory((Class<? extends Node>) template);
		}

		return null;
	}
}
