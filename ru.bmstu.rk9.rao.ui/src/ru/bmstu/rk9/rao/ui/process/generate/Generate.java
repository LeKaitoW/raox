package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Generate extends NodeWithProperty {

	private static final long serialVersionUID = 1;
	
	public Generate() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.lightBlue;
	public static String name = "Generate";

	@Override
	public Block createBlock() {
		ru.bmstu.rk9.rao.lib.process.Generate generate = new ru.bmstu.rk9.rao.lib.process.Generate(() -> 10);
		return generate;
	}
}
