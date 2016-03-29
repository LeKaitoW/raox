package ru.bmstu.rk9.rao.ui.process.advance;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithInterval;

public class Advance extends NodeWithInterval implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Advance() {
		super(backgroundColor.getRGB(), "Advance");
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_OUT, 0);
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Advance";

	@Override
	public BlockConverterInfo createBlock() {
		BlockConverterInfo advanceInfo = new BlockConverterInfo();
		Double interval;
		try {
			interval = Double.valueOf(this.interval);
		} catch (NumberFormatException e) {
			advanceInfo.isSuccessful = false;
			advanceInfo.errorMessage = e.getMessage();
			System.out.println(advanceInfo.errorMessage);
			return advanceInfo;
		}
		ru.bmstu.rk9.rao.lib.process.Advance advance = new ru.bmstu.rk9.rao.lib.process.Advance(() -> interval);
		advanceInfo.setBlock(advance);
		advanceInfo.inputDocks.put(TERMINAL_IN, advance.getInputDock());
		advanceInfo.outputDocks.put(TERMINAL_OUT, advance.getOutputDock());
		return advanceInfo;
	}

}
