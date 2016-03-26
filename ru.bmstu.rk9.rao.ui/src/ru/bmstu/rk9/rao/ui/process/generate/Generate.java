package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithInterval;

public class Generate extends NodeWithInterval {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_OUT = "OUT";

	public Generate() {
		super(backgroundColor.getRGB(), "Interval");
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Generate";

	@Override
	public BlockConverterInfo createBlock() {
		BlockConverterInfo generateInfo = new BlockConverterInfo();
		Double interval;
		try {
			interval = Double.valueOf(this.interval);
		} catch (NumberFormatException e) {
			generateInfo.isSuccessful = false;
			generateInfo.errorMessage = e.getMessage();
			System.out.println(generateInfo.errorMessage);
			return generateInfo;
		}
		ru.bmstu.rk9.rao.lib.process.Generate generate = new ru.bmstu.rk9.rao.lib.process.Generate(() -> interval);
		generateInfo.setBlock(generate);
		generateInfo.outputDocks.put(TERMINAL_OUT, generate.getOutputDock());
		return generateInfo;
	}
}
