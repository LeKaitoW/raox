package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithInterval;

public class Generate extends NodeWithInterval {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_OUT = "OUT";

	public Generate() {
		super(backgroundColor.getRGB());
		intervalName = "Interval";
	}

	private static Color backgroundColor = ColorConstants.lightBlue;
	public static String name = "Generate";

	@Override
	public BlockConverterInfo createBlock() {
		ru.bmstu.rk9.rao.lib.process.Generate generate = new ru.bmstu.rk9.rao.lib.process.Generate(
				() -> Double.valueOf(this.interval));
		BlockConverterInfo generateInfo = new BlockConverterInfo(generate);
		generateInfo.outputDocks.put(TERMINAL_OUT, generate.getOutputDock());
		return generateInfo;
	}
}
