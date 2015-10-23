package ru.bmstu.rk9.rao.ui.process.resource;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.Node;

public class Resource extends Node {

	public Resource() {
		super(backgroundColor);
	}

	private static Color backgroundColor = ColorConstants.cyan;
}
