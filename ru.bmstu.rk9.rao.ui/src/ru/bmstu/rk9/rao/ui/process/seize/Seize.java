package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Seize extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public Seize() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.darkGray;
	public static String name = "Seize";

	@Override
	public BlockConverterInfo createBlock() {
		return null;
	}
}
