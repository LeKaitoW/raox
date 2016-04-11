package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.node.Node;

public class ModelNode extends Node {

	private static final long serialVersionUID = 1;

	private static Color foregroundColor = ColorConstants.white;
	public static String name = "Model";
	private transient EResourceRetriever resourceRetriever;

	public ModelNode() {
		super(foregroundColor.getRGB());
	}

	public final EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}

	public final void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}
}
