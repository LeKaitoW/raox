package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Terminate extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public Terminate() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.red;
	public static String name = "Terminate";

}
