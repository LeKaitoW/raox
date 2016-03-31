package ru.bmstu.rk9.rao.ui.process.selectpath;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProbability;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;

public class SelectPath extends NodeWithProbability implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_TRUE_OUT = "TRUE_OUT";
	public static final String TERMINAL_FALSE_OUT = "FALSE_OUT";

	public SelectPath() {
		super(foregroundColor.getRGB());
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_FALSE_OUT, 0);
		linksCount.put(TERMINAL_TRUE_OUT, 0);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "SelectPath";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.SelectPath selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(
				Double.valueOf(this.probability));
		BlockConverterInfo selectPathInfo = new BlockConverterInfo();
		selectPathInfo.setBlock(selectPath);
		selectPathInfo.inputDocks.put(TERMINAL_IN, selectPath.getInputDock());
		selectPathInfo.outputDocks.put(TERMINAL_TRUE_OUT, selectPath.getTrueOutputDock());
		selectPathInfo.outputDocks.put(TERMINAL_FALSE_OUT, selectPath.getFalseOutputDock());
		return selectPathInfo;
	}
}
