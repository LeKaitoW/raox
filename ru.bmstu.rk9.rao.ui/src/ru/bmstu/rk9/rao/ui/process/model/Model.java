package ru.bmstu.rk9.rao.ui.process.model;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.Node;

public class Model extends Node {

	public Model() {
		super(backgroundColor);
	}

	private static Color backgroundColor = ColorConstants.white;
	public static String name = "Model";
}
