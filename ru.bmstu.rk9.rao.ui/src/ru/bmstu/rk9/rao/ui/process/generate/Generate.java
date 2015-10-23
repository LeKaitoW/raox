package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.Node;

public class Generate extends Node {

	public Generate() {
		super(backgroundColor);
	}

	private static Color backgroundColor = ColorConstants.lightBlue;
	public static String name = "Generate";
}
