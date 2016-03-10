package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Release extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Release() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = ColorConstants.yellow;
	public static String name = "Release";

	@Override
	public BlockConverterInfo createBlock() {
		return null;
	}
}
