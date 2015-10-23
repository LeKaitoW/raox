package ru.bmstu.rk9.rao.ui.process.seize;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.Node;

public class Seize extends Node {

	public Seize() {
		super(backgroundColor);
	}

	private static Color backgroundColor = ColorConstants.darkGray;
	public static String name = "Seize";
}
