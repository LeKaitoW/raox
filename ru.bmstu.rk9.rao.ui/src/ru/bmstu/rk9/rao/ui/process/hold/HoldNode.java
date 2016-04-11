package ru.bmstu.rk9.rao.ui.process.hold;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.node.BlockNodeWithInterval;

public class HoldNode extends BlockNodeWithInterval implements Serializable {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";
	public static String name = "Hold";

	public HoldNode() {
		super("Hold");
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

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
		holdInfo.inputDocks.put(DOCK_IN, hold.getInputDock());
		holdInfo.outputDocks.put(DOCK_OUT, hold.getOutputDock());
		return holdInfo;
	}

	@Override
	public void validateProperty(IResource file) throws CoreException {
		validateInterval(file);
		validateConnections(file, 1, 1);
	}
}
