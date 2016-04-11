package ru.bmstu.rk9.rao.ui.process.generate;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.node.BlockNodeWithInterval;

public class GenerateNode extends BlockNodeWithInterval {

	private static final long serialVersionUID = 1;

	public static final String DOCK_OUT = "OUT";
	public static String name = "Generate";

	public GenerateNode() {
		super("Interval");
		registerDock(DOCK_OUT);
	}

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
		generateInfo.outputDocks.put(DOCK_OUT, generate.getOutputDock());
		return generateInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateInterval(file);
		validateConnections(file, 1, 0);
	}
}
