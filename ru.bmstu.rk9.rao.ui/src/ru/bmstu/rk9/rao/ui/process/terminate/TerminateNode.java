package ru.bmstu.rk9.rao.ui.process.terminate;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithProperty;

public class TerminateNode extends NodeWithProperty {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";

	public TerminateNode() {
		super(foregroundColor.getRGB());
		registerDock(DOCK_IN);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Terminate";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Terminate terminate = new ru.bmstu.rk9.rao.lib.process.Terminate();
		BlockConverterInfo terminateInfo = new BlockConverterInfo();
		terminateInfo.setBlock(terminate);
		terminateInfo.inputDocks.put(DOCK_IN, terminate.getInputDock());
		return terminateInfo;
	}

}
