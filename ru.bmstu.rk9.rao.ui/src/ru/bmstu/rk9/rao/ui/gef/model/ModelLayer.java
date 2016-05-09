package ru.bmstu.rk9.rao.ui.gef.model;

import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.XYLayout;

public class ModelLayer extends Layer {

	public ModelLayer() {
		setLayoutManager(new XYLayout());
		setOpaque(false);
	}
}
