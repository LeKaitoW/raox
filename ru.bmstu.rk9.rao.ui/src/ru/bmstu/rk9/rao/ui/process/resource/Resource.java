package ru.bmstu.rk9.rao.ui.process.resource;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Resource extends NodeWithProperty {

	private static final long serialVersionUID = 1;
	
	public Resource() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.cyan;
	public static String name = "Resource";

	@Override
	public BlockConverterInfo createBlock() {
		return null;
	}
}
