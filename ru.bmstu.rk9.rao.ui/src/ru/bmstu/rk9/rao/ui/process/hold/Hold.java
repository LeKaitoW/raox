package ru.bmstu.rk9.rao.ui.process.hold;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithInterval;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;

public class Hold extends NodeWithInterval implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Hold() {
		super(foregroundColor.getRGB(), "Hold");
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_OUT, 0);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Hold";

	@Override
	public BlockConverterInfo createBlock() {
		BlockConverterInfo holdInfo = new BlockConverterInfo();
		Double interval;
		try {
			interval = Double.valueOf(this.interval);
		} catch (NumberFormatException e) {
			holdInfo.isSuccessful = false;
			holdInfo.errorMessage = e.getMessage();
			System.out.println(holdInfo.errorMessage);
			return holdInfo;
		}
		ru.bmstu.rk9.rao.lib.process.Hold hold = new ru.bmstu.rk9.rao.lib.process.Hold(() -> interval);
		holdInfo.setBlock(hold);
		holdInfo.inputDocks.put(TERMINAL_IN, hold.getInputDock());
		holdInfo.outputDocks.put(TERMINAL_OUT, hold.getOutputDock());
		return holdInfo;
	}

}
