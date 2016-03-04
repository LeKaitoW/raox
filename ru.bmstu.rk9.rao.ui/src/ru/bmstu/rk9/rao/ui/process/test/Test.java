package ru.bmstu.rk9.rao.ui.process.test;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.process.Block;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Test extends NodeWithProperty implements Serializable {

	private static final long serialVersionUID = 1;

	public Test() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.cyan;
	public static String name = "Test";

	@Override
	public Block createBlock() {
		ru.bmstu.rk9.rao.lib.process.Test test = new ru.bmstu.rk9.rao.lib.process.Test();
		return test;
	}
}
