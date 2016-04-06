package ru.bmstu.rk9.rao.ui.process.seize;

import java.util.Optional;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithResource;

public class Seize extends NodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";

	public Seize() {
		super(foregroundColor.getRGB());
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Seize";

	@Override
	public BlockConverterInfo createBlock() {
		Optional<Resource> optional = Simulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny();
		BlockConverterInfo seizeInfo = new BlockConverterInfo();
		if (!optional.isPresent()) {
			seizeInfo.isSuccessful = false;
			seizeInfo.errorMessage = "Resource name not selected";
			resourceName = "";
			return seizeInfo;
		}
		Resource releasedResource = optional.get();
		ru.bmstu.rk9.rao.lib.process.Seize seize = new ru.bmstu.rk9.rao.lib.process.Seize(releasedResource);
		seizeInfo.setBlock(seize);
		seizeInfo.inputDocks.put(DOCK_IN, seize.getInputDock());
		seizeInfo.outputDocks.put(DOCK_OUT, seize.getOutputDock());
		return seizeInfo;
	}
}
