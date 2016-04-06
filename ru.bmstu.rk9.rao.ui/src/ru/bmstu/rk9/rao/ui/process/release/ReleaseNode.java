package ru.bmstu.rk9.rao.ui.process.release;

import java.util.Optional;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.ProcessColors;
import ru.bmstu.rk9.rao.ui.process.node.NodeWithResource;

public class ReleaseNode extends NodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String DOCK_IN = "IN";
	public static final String DOCK_OUT = "OUT";

	public ReleaseNode() {
		super(foregroundColor.getRGB());
		registerDock(DOCK_IN);
		registerDock(DOCK_OUT);
	}

	private static Color foregroundColor = ProcessColors.BLOCK_COLOR;
	public static String name = "Release";

	@Override
	public BlockConverterInfo createBlock() {
		Optional<Resource> optional = Simulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny();
		BlockConverterInfo releaseInfo = new BlockConverterInfo();
		if (!optional.isPresent()) {
			releaseInfo.isSuccessful = false;
			releaseInfo.errorMessage = "Resource name not selected";
			resourceName = "";
			return releaseInfo;
		}
		Resource seizedResource = optional.get();
		ru.bmstu.rk9.rao.lib.process.Release release = new ru.bmstu.rk9.rao.lib.process.Release(seizedResource);
		releaseInfo.setBlock(release);
		releaseInfo.inputDocks.put(DOCK_IN, release.getInputDock());
		releaseInfo.outputDocks.put(DOCK_OUT, release.getOutputDock());
		return releaseInfo;
	}
}
