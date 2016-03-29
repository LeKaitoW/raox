package ru.bmstu.rk9.rao.ui.process.test;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithProbability;

public class Test extends NodeWithProbability implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_TRUE_OUT = "TRUE_OUT";
	public static final String TERMINAL_FALSE_OUT = "FALSE_OUT";

	public Test() {
		super(backgroundColor.getRGB());
		linksCount.put(TERMINAL_IN, 0);
		linksCount.put(TERMINAL_FALSE_OUT, 0);
		linksCount.put(TERMINAL_TRUE_OUT, 0);
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Test";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Test test = new ru.bmstu.rk9.rao.lib.process.Test(
				Double.valueOf(this.probability));
		BlockConverterInfo testInfo = new BlockConverterInfo();
		testInfo.setBlock(test);
		testInfo.inputDocks.put(TERMINAL_IN, test.getInputDock());
		testInfo.outputDocks.put(TERMINAL_TRUE_OUT, test.getTrueOutputDock());
		testInfo.outputDocks.put(TERMINAL_FALSE_OUT, test.getFalseOutputDock());
		return testInfo;
	}
}
