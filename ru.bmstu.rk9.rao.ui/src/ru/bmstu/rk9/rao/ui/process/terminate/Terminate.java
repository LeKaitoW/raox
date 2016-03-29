package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProperty;

public class Terminate extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";

	public Terminate() {
		super(backgroundColor.getRGB());
		linksCount.put(TERMINAL_IN, 0);
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Terminate";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Terminate terminate = new ru.bmstu.rk9.rao.lib.process.Terminate();
		BlockConverterInfo terminateInfo = new BlockConverterInfo();
		terminateInfo.setBlock(terminate);
		terminateInfo.inputDocks.put(TERMINAL_IN, terminate.getInputDock());
		return terminateInfo;
	}

}
