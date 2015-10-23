package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.Node;

public class Release extends Node {

	public Release() {
		super(backgroundColor);
	}

	private static Color backgroundColor = ColorConstants.yellow;
	public static String name = "Release";
}
