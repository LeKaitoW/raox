package ru.bmstu.rk9.rao.ui.process.advance;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithInterval;

public class Advance extends NodeWithInterval implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Advance() {
		super(backgroundColor.getRGB());
		intervalName = "Advance";
	}

	private static Color backgroundColor = ColorConstants.darkGreen;
	public static String name = "Advance";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Advance advance = new ru.bmstu.rk9.rao.lib.process.Advance(
				() -> Double.valueOf(this.interval));
		BlockConverterInfo advanceInfo = new BlockConverterInfo(advance);
		advanceInfo.inputDocks.put(TERMINAL_IN, advance.getInputDock());
		advanceInfo.outputDocks.put(TERMINAL_OUT, advance.getOutputDock());
		return advanceInfo;
	}

}
