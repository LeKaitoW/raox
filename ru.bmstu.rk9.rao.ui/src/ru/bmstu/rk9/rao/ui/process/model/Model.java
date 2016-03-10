package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.Node;

public class Model extends Node {

	private static final long serialVersionUID = 1;

	public Model() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.white;
	public static String name = "Model";

	@Override
	public BlockConverterInfo createBlock() {
		return null;
	}
}
