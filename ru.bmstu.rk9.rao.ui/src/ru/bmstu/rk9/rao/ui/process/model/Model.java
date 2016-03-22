package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.Node;

public class Model extends Node {

	private static final long serialVersionUID = 1;

	public Model() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.white;
	public static String name = "Model";

	private transient EResourceRetriever resourceRetriever;

	@Override
	public BlockConverterInfo createBlock() {
		return null;
	}

	public void setResourceRetriever(EResourceRetriever resourceRetriever) {
		this.resourceRetriever = resourceRetriever;
	}

	public EResourceRetriever getResourceRetriever() {
		return resourceRetriever;
	}
}
