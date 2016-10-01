package ru.bmstu.rk9.rao.ui.gef.model;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;

public class ModelLayer extends FreeformLayer {

	public ModelLayer() {
		setLayoutManager(new FreeformLayout());
		setOpaque(false);
	}
}
