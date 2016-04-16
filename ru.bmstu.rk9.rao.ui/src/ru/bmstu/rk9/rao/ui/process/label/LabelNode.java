package ru.bmstu.rk9.rao.ui.process.label;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;

import ru.bmstu.rk9.rao.ui.process.node.Node;

public class LabelNode extends Node implements Serializable {

	private static final long serialVersionUID = 1;

	public static String name = "Label";

	public LabelNode() {
		super(ColorConstants.yellow.getRGB());
	}
}
