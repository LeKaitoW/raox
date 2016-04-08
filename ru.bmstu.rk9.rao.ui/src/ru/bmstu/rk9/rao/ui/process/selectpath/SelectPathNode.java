package ru.bmstu.rk9.rao.ui.process.selectpath;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithProbability;

public class SelectPathNode extends NodeWithProbability implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_TRUE_OUT = "TRUE_OUT";
	public static final String DOCK_FALSE_OUT = "FALSE_OUT";

	public SelectPathNode() {
		super(foregroundColor.getRGB());
		setName(name);
		registerDock(DOCK_IN);
		registerDock(DOCK_FALSE_OUT);
		registerDock(DOCK_TRUE_OUT);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "SelectPath";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.SelectPath selectPath = new ru.bmstu.rk9.rao.lib.process.SelectPath(
				Double.valueOf(this.probability));
		BlockConverterInfo selectPathInfo = new BlockConverterInfo();
		selectPathInfo.setBlock(selectPath);
		selectPathInfo.inputDocks.put(DOCK_IN, selectPath.getInputDock());
		selectPathInfo.outputDocks.put(DOCK_TRUE_OUT, selectPath.getTrueOutputDock());
		selectPathInfo.outputDocks.put(DOCK_FALSE_OUT, selectPath.getFalseOutputDock());
		return selectPathInfo;
	}
}
