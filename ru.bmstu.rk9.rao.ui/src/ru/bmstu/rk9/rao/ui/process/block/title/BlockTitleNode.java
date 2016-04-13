package ru.bmstu.rk9.rao.ui.process.block.title;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;

import ru.bmstu.rk9.rao.ui.process.node.Node;

public class BlockTitleNode extends Node implements Serializable {

	private static final long serialVersionUID = 1;

	public static String name = "Title";

	public BlockTitleNode() {
		super(ColorConstants.red.getRGB());
	}
}
