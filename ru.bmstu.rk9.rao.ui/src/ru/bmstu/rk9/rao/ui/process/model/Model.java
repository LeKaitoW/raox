package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.EResourceRetriever;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithConnections;

public class Model extends NodeWithConnections {

	private static final long serialVersionUID = 1;

	public Model() {
		super(foregroundColor.getRGB());
	}

	private static Color foregroundColor = ColorConstants.white;
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
