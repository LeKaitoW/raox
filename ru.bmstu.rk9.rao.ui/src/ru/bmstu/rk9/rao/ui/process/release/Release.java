package ru.bmstu.rk9.rao.ui.process.release;

import org.eclipse.swt.graphics.Color;

import ru.bmstu.rk9.rao.lib.resource.Resource;
import ru.bmstu.rk9.rao.lib.simulator.Simulator;
import ru.bmstu.rk9.rao.ui.process.BlockConverterInfo;
import ru.bmstu.rk9.rao.ui.process.NodeWithResource;

public class Release extends NodeWithResource {

	private static final long serialVersionUID = 1;

	public static final String TERMINAL_IN = "IN";
	public static final String TERMINAL_OUT = "OUT";

	public Release() {
		super(backgroundColor.getRGB());
	}

	private static Color backgroundColor = new Color(null, 67, 181, 129);
	public static String name = "Release";

	@Override
	public BlockConverterInfo createBlock() {
		BlockConverterInfo releaseInfo = new BlockConverterInfo();
		if (this.resourceName.isEmpty()) {
			releaseInfo.isSuccessful = false;
			releaseInfo.errorMessage = "Resource name not selected";
			return releaseInfo;
		}
		Resource seizedResource = Simulator.getModelState().getAllResources().stream()
				.filter(resource -> resource.getName().equals(resourceName)).findAny().get();
		ru.bmstu.rk9.rao.lib.process.Release release = new ru.bmstu.rk9.rao.lib.process.Release(seizedResource);
		releaseInfo.setBlock(release);
		releaseInfo.inputDocks.put(TERMINAL_IN, release.getInputDock());
		releaseInfo.outputDocks.put(TERMINAL_OUT, release.getOutputDock());
		return releaseInfo;
	}
}
